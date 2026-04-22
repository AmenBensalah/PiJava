package edu.PROJETPI.services;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

public class InvoicePdfService {

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float LEFT = 80f;
    private static final float RIGHT = PAGE_WIDTH - 80f;
    private static final float LINE_HEIGHT = 22f;
    private static final DecimalFormat MONEY_FORMAT =
            new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String LOGO_RESOURCE = "/logo.jpg";
    private static final Path LOGO_FALLBACK_PATH = Paths.get("out", "logo.jpg");

    public Path generateInvoice(Commande commande, List<CartItem> articles, double total) throws IOException {
        Path pdfPath = Files.createTempFile("facture-", ".pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawLogoWatermark(document, content);
                float currentY = drawHeader(document, content, commande);
                currentY = drawClientBlock(content, commande, currentY);
                currentY = drawOrderDetails(content, articles, total, currentY);
                drawFooter(content, currentY);
            }

            document.save(pdfPath.toFile());
        }

        return pdfPath;
    }

    public void openInvoice(Path pdfPath) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(pdfPath.toFile());
            return;
        }
        throw new IOException("Ouverture automatique du PDF non supportee sur cette machine.");
    }

    private float drawHeader(PDDocument document, PDPageContentStream content, Commande commande) throws IOException {
        boolean logoDrawn = drawLogo(document, content);
        float titleY = logoDrawn ? 620f : 720f;

        if (!logoDrawn) {
            drawCenteredText(content, "E-SPORTIFY", 750f, PDType1Font.HELVETICA_BOLD_OBLIQUE, 26);
        }

        drawCenteredText(content, "FACTURE", titleY, PDType1Font.HELVETICA_BOLD, 28);

        String dateText = "Date: " + LocalDateTime.now().format(DATE_FORMAT);
        String referenceText = "Reference: " + buildReference(commande);

        float metaY = titleY - 34f;
        drawRightAlignedText(content, dateText, RIGHT, metaY, PDType1Font.HELVETICA, 12);
        drawRightAlignedText(content, referenceText, RIGHT, metaY - 20f, PDType1Font.HELVETICA, 12);
        return metaY - 34f;
    }

    private boolean drawLogo(PDDocument document, PDPageContentStream content) throws IOException {
        BufferedImage image = loadLogoImage();
        if (image == null) {
            return false;
        }

        image = cropLogoImage(image);
        PDImageXObject logo = JPEGFactory.createFromImage(document, image);
        float logoWidth = 180f;
        float scale = logoWidth / image.getWidth();
        float logoHeight = image.getHeight() * scale;
        float x = (PAGE_WIDTH - logoWidth) / 2;
        float y = PAGE_HEIGHT - logoHeight - 24f;
        content.drawImage(logo, x, y, logoWidth, logoHeight);
        return true;
    }

    private void drawLogoWatermark(PDDocument document, PDPageContentStream content) throws IOException {
        BufferedImage image = loadLogoImage();
        if (image == null) {
            return;
        }

        image = cropLogoImage(image);
        PDImageXObject logo = JPEGFactory.createFromImage(document, image);
        float logoWidth = 330f;
        float scale = logoWidth / image.getWidth();
        float logoHeight = image.getHeight() * scale;
        float x = (PAGE_WIDTH - logoWidth) / 2;
        float y = 235f;

        content.saveGraphicsState();
        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
        graphicsState.setNonStrokingAlphaConstant(0.09f);
        content.setGraphicsStateParameters(graphicsState);
        content.drawImage(logo, x, y, logoWidth, logoHeight);
        content.restoreGraphicsState();
    }

    private BufferedImage loadLogoImage() throws IOException {
        try (InputStream resourceStream = InvoicePdfService.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (resourceStream != null) {
                return ImageIO.read(resourceStream);
            }
        }

        if (Files.exists(LOGO_FALLBACK_PATH)) {
            try (InputStream fileStream = Files.newInputStream(LOGO_FALLBACK_PATH)) {
                return ImageIO.read(fileStream);
            }
        }

        return null;
    }

    private BufferedImage cropLogoImage(BufferedImage source) {
        int minX = source.getWidth();
        int minY = source.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;
                int red = (argb >> 16) & 0xff;
                int green = (argb >> 8) & 0xff;
                int blue = argb & 0xff;

                boolean hasContent = alpha > 10;
                boolean nearWhite = red > 245 && green > 245 && blue > 245;
                if (hasContent && !nearWhite) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return source;
        }

        int padding = 8;
        int croppedX = Math.max(0, minX - padding);
        int croppedY = Math.max(0, minY - padding);
        int croppedWidth = Math.min(source.getWidth() - croppedX, (maxX - minX) + (padding * 2) + 1);
        int croppedHeight = Math.min(source.getHeight() - croppedY, (maxY - minY) + (padding * 2) + 1);
        return source.getSubimage(croppedX, croppedY, croppedWidth, croppedHeight);
    }

    private float drawClientBlock(PDPageContentStream content, Commande commande, float top) throws IOException {
        float bottom = top - 95f;
        float splitX = LEFT + 135f;

        drawRectangle(content, LEFT, bottom, RIGHT - LEFT, top - bottom);
        drawVerticalLine(content, splitX, bottom, top);

        writeText(content, "Client", LEFT + 8, top - 18, PDType1Font.HELVETICA_BOLD, 16);

        float labelX = LEFT + 8;
        float valueX = splitX + 8;
        float y = top - 42;

        writeText(content, "Nom complet:", labelX, y, PDType1Font.HELVETICA, 12);
        writeText(content, buildFullName(commande), valueX, y, PDType1Font.HELVETICA, 12);

        y -= LINE_HEIGHT;
        writeText(content, "Telephone:", labelX, y, PDType1Font.HELVETICA, 12);
        writeText(content, safe(commande.getTelephone()), valueX, y, PDType1Font.HELVETICA, 12);

        y -= LINE_HEIGHT;
        writeText(content, "Adresse:", labelX, y, PDType1Font.HELVETICA, 12);
        writeText(content, safe(commande.getAdresse()), valueX, y, PDType1Font.HELVETICA, 12);
        return bottom - 34f;
    }

    private float drawOrderDetails(PDPageContentStream content, List<CartItem> articles, double total, float startY) throws IOException {
        writeText(content, "Details de la commande", LEFT, startY, PDType1Font.HELVETICA_BOLD, 18);
        writeText(content, "Articles: " + articles.stream().mapToInt(CartItem::getQuantite).sum(), LEFT, startY - 28f, PDType1Font.HELVETICA, 12);

        float tableTop = startY - 54f;
        float headerBottom = tableTop - 40f;
        float rowHeight = 30f;
        float productWidth = 220f;
        float qtyWidth = 60f;
        float unitWidth = 110f;
        float subtotalWidth = RIGHT - LEFT - productWidth - qtyWidth - unitWidth;

        float productX = LEFT;
        float qtyX = productX + productWidth;
        float unitX = qtyX + qtyWidth;
        float subtotalX = unitX + unitWidth;
        float bottom = headerBottom - (articles.size() * rowHeight);

        drawRectangle(content, LEFT, bottom, RIGHT - LEFT, tableTop - bottom);
        drawHorizontalLine(content, LEFT, RIGHT, headerBottom);
        drawVerticalLine(content, qtyX, bottom, tableTop);
        drawVerticalLine(content, unitX, bottom, tableTop);
        drawVerticalLine(content, subtotalX, bottom, tableTop);

        writeText(content, "Produit", productX + 8, tableTop - 22f, PDType1Font.HELVETICA_BOLD, 14);
        drawCenteredTextInCell(content, "Qt", qtyX, qtyWidth, tableTop - 22f, PDType1Font.HELVETICA_BOLD, 14);
        drawCenteredTextInCell(content, "PU", unitX, unitWidth, tableTop - 16f, PDType1Font.HELVETICA_BOLD, 14);
        drawCenteredTextInCell(content, "(TND)", unitX, unitWidth, tableTop - 32f, PDType1Font.HELVETICA_BOLD, 11);
        drawCenteredTextInCell(content, "Sous-total", subtotalX, subtotalWidth, tableTop - 16f, PDType1Font.HELVETICA_BOLD, 14);
        drawCenteredTextInCell(content, "(TND)", subtotalX, subtotalWidth, tableTop - 32f, PDType1Font.HELVETICA_BOLD, 11);

        float currentY = headerBottom - 21;
        for (int i = 0; i < articles.size(); i++) {
            CartItem article = articles.get(i);
            if (i > 0) {
                float separatorY = headerBottom - (i * rowHeight);
                drawHorizontalLine(content, LEFT, RIGHT, separatorY);
            }

            writeText(content, safe(article.getNomProduit()), productX + 8, currentY, PDType1Font.HELVETICA, 12);
            drawCenteredTextInCell(content, String.valueOf(article.getQuantite()), qtyX, qtyWidth, currentY, PDType1Font.HELVETICA, 12);
            drawCenteredTextInCell(content, formatMoney(article.getPrixUnitaire()), unitX, unitWidth, currentY, PDType1Font.HELVETICA, 12);
            drawCenteredTextInCell(content, formatMoney(article.getSousTotal()), subtotalX, subtotalWidth, currentY, PDType1Font.HELVETICA, 12);
            currentY -= rowHeight;
        }

        float totalBoxWidth = 250f;
        float totalBoxHeight = 34f;
        float totalBoxX = RIGHT - totalBoxWidth;
        float totalBoxY = bottom - 52f;

        drawRectangle(content, totalBoxX, totalBoxY, totalBoxWidth, totalBoxHeight);
        writeText(content, "Montant total paye: " + formatMoney(total) + " TND",
                totalBoxX + 10, totalBoxY + 12, PDType1Font.HELVETICA_BOLD, 12);
        return totalBoxY - 44f;
    }

    private void drawFooter(PDPageContentStream content, float y) throws IOException {
        drawCenteredText(content, "\"Merci pour votre confiance.\"", Math.max(y, 55f), PDType1Font.HELVETICA, 16);
    }

    private void drawRectangle(PDPageContentStream content, float x, float y, float width, float height) throws IOException {
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private void drawVerticalLine(PDPageContentStream content, float x, float yStart, float yEnd) throws IOException {
        content.moveTo(x, yStart);
        content.lineTo(x, yEnd);
        content.stroke();
    }

    private void drawHorizontalLine(PDPageContentStream content, float xStart, float xEnd, float y) throws IOException {
        content.moveTo(xStart, y);
        content.lineTo(xEnd, y);
        content.stroke();
    }

    private void drawCenteredText(PDPageContentStream content, String text, float y,
                                  PDType1Font font, float fontSize) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (PAGE_WIDTH - textWidth) / 2;
        writeText(content, text, x, y, font, fontSize);
    }

    private void drawRightAlignedText(PDPageContentStream content, String text, float rightX, float y,
                                      PDType1Font font, float fontSize) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        writeText(content, text, rightX - textWidth, y, font, fontSize);
    }

    private void drawCenteredTextInCell(PDPageContentStream content, String text, float cellX, float cellWidth, float y,
                                        PDType1Font font, float fontSize) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = cellX + ((cellWidth - textWidth) / 2);
        writeText(content, text, x, y, font, fontSize);
    }

    private void writeText(PDPageContentStream content, String text, float x, float y,
                           PDType1Font font, float fontSize) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(sanitize(text));
        content.endText();
    }

    private String buildFullName(Commande commande) {
        String fullName = (safe(commande.getNom()) + " " + safe(commande.getPrenom())).trim();
        return fullName.isBlank() ? "-" : fullName;
    }

    private String sanitize(String value) {
        return value
                .replace("’", "'")
                .replace("–", "-")
                .replace("—", "-");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatMoney(double amount) {
        return MONEY_FORMAT.format(amount);
    }

    private String buildReference(Commande commande) {
        if (commande != null && commande.getId() > 0) {
            return "CMD-" + commande.getId();
        }
        return "CMD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}

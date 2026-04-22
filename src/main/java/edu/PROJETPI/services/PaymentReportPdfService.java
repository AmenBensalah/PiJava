package edu.PROJETPI.services;

import edu.PROJETPI.AdminDashboardController;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.awt.Desktop;
import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PaymentReportPdfService {

    private static final DecimalFormat MONEY_FORMAT =
            new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE));
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 42f;
    private static final float TABLE_TOP = 605f;
    private static final float ROW_HEIGHT = 24f;
    private static final Color TITLE_COLOR = new Color(34, 60, 82);
    private static final Color TABLE_HEADER_COLOR = new Color(228, 228, 228);
    private static final Color LINE_COLOR = new Color(125, 125, 125);
    private static final Color STRIPE_RED = new Color(184, 18, 23);
    private static final Color STRIPE_ORANGE = new Color(247, 126, 22);

    public Path generateReport(List<AdminDashboardController.PaymentRow> payments,
                               LocalDate dateDebut,
                               LocalDate dateFin,
                               String adminName) throws IOException {
        Path pdfPath = Files.createTempFile("rapport-paiements-", ".pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PDImageXObject logo = loadLogo(document);
                drawPageBackground(content);
                drawHeader(content, logo, dateDebut, dateFin, adminName, payments);
                drawWatermark(content, logo);
                drawTable(content, payments);
                drawSummary(content, payments);
                drawBottomStripe(content);
            }

            document.save(pdfPath.toFile());
        }

        return pdfPath;
    }

    public void openReport(Path pdfPath) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(pdfPath.toFile());
            return;
        }
        throw new IOException("Ouverture automatique du PDF non supportee sur cette machine.");
    }

    public void printReport(Path pdfPath) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.PRINT)) {
            Desktop.getDesktop().print(pdfPath.toFile());
            return;
        }
        openReport(pdfPath);
    }

    private void drawHeader(PDPageContentStream content,
                            PDImageXObject logo,
                            LocalDate dateDebut,
                            LocalDate dateFin,
                            String adminName,
                            List<AdminDashboardController.PaymentRow> payments) throws IOException {
        writeCentered(content, "Rapport des paiements", PAGE_HEIGHT - 50f, PDType1Font.HELVETICA_BOLD, 23, TITLE_COLOR);
        writeText(content, "Periode choisie : " + buildPeriodLabel(dateDebut, dateFin), MARGIN, PAGE_HEIGHT - 92f,
                PDType1Font.HELVETICA, 12);
        writeText(content, "Date de generation : " + LocalDateTime.now().format(DATE_TIME_FORMAT), MARGIN, PAGE_HEIGHT - 112f,
                PDType1Font.HELVETICA, 12);
        writeText(content, "Admin connecte : " + safe(adminName), MARGIN, PAGE_HEIGHT - 132f,
                PDType1Font.HELVETICA, 12);
        writeText(content, "Paiements affiches : " + payments.size(), MARGIN, PAGE_HEIGHT - 152f,
                PDType1Font.HELVETICA, 12);

        if (logo != null) {
            content.drawImage(logo, 255f, PAGE_HEIGHT - 178f, 140f, 112f);
        }

        setStroke(content, LINE_COLOR, 0.8f);
        drawHorizontalLine(content, MARGIN, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 205f);
    }

    private void drawPageBackground(PDPageContentStream content) throws IOException {
        content.setNonStrokingColor(Color.WHITE);
        content.addRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
        content.fill();
    }

    private void drawTable(PDPageContentStream content, List<AdminDashboardController.PaymentRow> payments) throws IOException {
        float idX = MARGIN;
        float commandeX = 90f;
        float montantX = 180f;
        float statutX = 315f;
        float dateX = 420f;
        float right = PAGE_WIDTH - MARGIN;
        float tableBottom = Math.max(120f, TABLE_TOP - ((payments.size() + 1) * ROW_HEIGHT));

        content.setNonStrokingColor(TABLE_HEADER_COLOR);
        content.addRect(MARGIN, TABLE_TOP - ROW_HEIGHT, right - MARGIN, ROW_HEIGHT);
        content.fill();

        setStroke(content, LINE_COLOR, 0.45f);
        drawRectangle(content, MARGIN, tableBottom, right - MARGIN, TABLE_TOP - tableBottom);
        drawHorizontalLine(content, MARGIN, right, TABLE_TOP - ROW_HEIGHT);
        drawVerticalLine(content, commandeX, tableBottom, TABLE_TOP);
        drawVerticalLine(content, montantX, tableBottom, TABLE_TOP);
        drawVerticalLine(content, statutX, tableBottom, TABLE_TOP);
        drawVerticalLine(content, dateX, tableBottom, TABLE_TOP);

        writeText(content, "ID", idX + 8, TABLE_TOP - 16f, PDType1Font.HELVETICA_BOLD, 12);
        writeText(content, "Commande", commandeX + 8, TABLE_TOP - 16f, PDType1Font.HELVETICA_BOLD, 12);
        writeText(content, "Montant", montantX + 8, TABLE_TOP - 16f, PDType1Font.HELVETICA_BOLD, 12);
        writeText(content, "Statut", statutX + 8, TABLE_TOP - 16f, PDType1Font.HELVETICA_BOLD, 12);
        writeText(content, "Date", dateX + 8, TABLE_TOP - 16f, PDType1Font.HELVETICA_BOLD, 12);

        float currentY = TABLE_TOP - 40f;
        for (int i = 0; i < payments.size(); i++) {
            AdminDashboardController.PaymentRow payment = payments.get(i);
            if (i > 0) {
                float separatorY = TABLE_TOP - ROW_HEIGHT - (i * ROW_HEIGHT);
                drawHorizontalLine(content, MARGIN, right, separatorY);
            }

            writeText(content, String.valueOf(payment.getId()), idX + 8, currentY, PDType1Font.HELVETICA, 11);
            writeText(content, String.valueOf(payment.getCommandeId()), commandeX + 8, currentY, PDType1Font.HELVETICA, 11);
            writeText(content, formatMoney(payment.getMontant()) + " TND", montantX + 8, currentY, PDType1Font.HELVETICA, 11);
            writeText(content, safe(payment.getStatut()), statutX + 8, currentY, PDType1Font.HELVETICA, 11);
            writeText(content, safe(payment.getDateLabel()), dateX + 8, currentY, PDType1Font.HELVETICA, 11);
            currentY -= ROW_HEIGHT;
        }
    }

    private void drawSummary(PDPageContentStream content, List<AdminDashboardController.PaymentRow> payments) throws IOException {
        double totalMontant = payments.stream().mapToDouble(AdminDashboardController.PaymentRow::getMontant).sum();
        float summaryY = 30f;
        float summaryHeight = 64f;
        setStroke(content, LINE_COLOR, 0.5f);
        drawRectangle(content, 30f, summaryY, PAGE_WIDTH - 60f, summaryHeight);
        writeText(content, "Total des paiements : " + payments.size(), MARGIN, summaryY + 38f, PDType1Font.HELVETICA_BOLD, 13);
        writeText(content, "Montant encaisse total : " + formatMoney(totalMontant) + " TND", MARGIN, summaryY + 16f,
                PDType1Font.HELVETICA_BOLD, 13);
    }

    private void drawWatermark(PDPageContentStream content, PDImageXObject logo) throws IOException {
        if (logo == null) {
            return;
        }

        content.saveGraphicsState();
        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
        graphicsState.setNonStrokingAlphaConstant(0.10f);
        content.setGraphicsStateParameters(graphicsState);
        content.drawImage(logo, 148f, 238f, 300f, 245f);
        content.restoreGraphicsState();
    }

    private void drawBottomStripe(PDPageContentStream content) throws IOException {
        content.setNonStrokingColor(STRIPE_RED);
        content.addRect(0, 0, PAGE_WIDTH * 0.54f, 6f);
        content.fill();
        content.setNonStrokingColor(STRIPE_ORANGE);
        content.addRect(PAGE_WIDTH * 0.54f, 0, PAGE_WIDTH * 0.46f, 6f);
        content.fill();
    }

    private PDImageXObject loadLogo(PDDocument document) throws IOException {
        URL resource = getClass().getResource("/logo.jpg");
        if (resource == null) {
            return null;
        }
        try {
            Path path = Paths.get(resource.toURI());
            return PDImageXObject.createFromFileByContent(path.toFile(), document);
        } catch (URISyntaxException e) {
            throw new IOException("Logo E-SPORTIFY introuvable.", e);
        }
    }

    private String buildPeriodLabel(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null && dateFin == null) {
            return "Toutes les periodes";
        }
        if (dateDebut != null && dateFin != null) {
            return dateDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " au " + dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        if (dateDebut != null) {
            return "A partir du " + dateDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "Jusqu'au " + dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void drawRectangle(PDPageContentStream content, float x, float y, float width, float height) throws IOException {
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private void setStroke(PDPageContentStream content, Color color, float width) throws IOException {
        content.setStrokingColor(color);
        content.setLineWidth(width);
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

    private void writeCentered(PDPageContentStream content, String text, float y, PDType1Font font, float fontSize)
            throws IOException {
        writeCentered(content, text, y, font, fontSize, Color.BLACK);
    }

    private void writeCentered(PDPageContentStream content, String text, float y, PDType1Font font, float fontSize, Color color)
            throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (PAGE_WIDTH - textWidth) / 2;
        writeText(content, text, x, y, font, fontSize, color);
    }

    private void writeText(PDPageContentStream content, String text, float x, float y, PDType1Font font, float fontSize)
            throws IOException {
        writeText(content, text, x, y, font, fontSize, Color.BLACK);
    }

    private void writeText(PDPageContentStream content, String text, float x, float y, PDType1Font font, float fontSize, Color color)
            throws IOException {
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(sanitize(text));
        content.endText();
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
}

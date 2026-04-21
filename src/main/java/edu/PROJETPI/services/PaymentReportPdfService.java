package edu.PROJETPI.services;

import edu.PROJETPI.AdminDashboardController;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final float TABLE_TOP = 615f;
    private static final float ROW_HEIGHT = 24f;

    public Path generateReport(List<AdminDashboardController.PaymentRow> payments,
                               LocalDate dateDebut,
                               LocalDate dateFin,
                               String adminName) throws IOException {
        Path pdfPath = Files.createTempFile("rapport-paiements-", ".pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawHeader(content, dateDebut, dateFin, adminName, payments);
                drawTable(content, payments);
                drawSummary(content, payments);
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
                            LocalDate dateDebut,
                            LocalDate dateFin,
                            String adminName,
                            List<AdminDashboardController.PaymentRow> payments) throws IOException {
        writeCentered(content, "Rapport des paiements", PAGE_HEIGHT - 52f, PDType1Font.HELVETICA_BOLD, 22);
        writeText(content, "Periode choisie : " + buildPeriodLabel(dateDebut, dateFin), MARGIN, PAGE_HEIGHT - 92f,
                PDType1Font.HELVETICA, 12);
        writeText(content, "Date de generation : " + LocalDateTime.now().format(DATE_TIME_FORMAT), MARGIN, PAGE_HEIGHT - 112f,
                PDType1Font.HELVETICA, 12);
        writeText(content, "Admin connecte : " + safe(adminName), MARGIN, PAGE_HEIGHT - 132f,
                PDType1Font.HELVETICA, 12);
        writeText(content, "Paiements affiches : " + payments.size(), MARGIN, PAGE_HEIGHT - 152f,
                PDType1Font.HELVETICA_BOLD, 12);
    }

    private void drawTable(PDPageContentStream content, List<AdminDashboardController.PaymentRow> payments) throws IOException {
        float idX = MARGIN;
        float commandeX = 90f;
        float montantX = 180f;
        float statutX = 315f;
        float dateX = 420f;
        float right = PAGE_WIDTH - MARGIN;
        float tableBottom = Math.max(120f, TABLE_TOP - ((payments.size() + 1) * ROW_HEIGHT));

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
        float y = 88f;
        writeText(content, "Total des paiements : " + payments.size(), MARGIN, y, PDType1Font.HELVETICA_BOLD, 13);
        writeText(content, "Montant encaisse total : " + formatMoney(totalMontant) + " TND", MARGIN, y - 22f,
                PDType1Font.HELVETICA_BOLD, 13);
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
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (PAGE_WIDTH - textWidth) / 2;
        writeText(content, text, x, y, font, fontSize);
    }

    private void writeText(PDPageContentStream content, String text, float x, float y, PDType1Font font, float fontSize)
            throws IOException {
        content.beginText();
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

package edu.PROJETPI;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.entites.Payment;
import edu.PROJETPI.services.ServiceCommande;
import edu.PROJETPI.services.ServicePayment;
import edu.PROJETPI.tools.MyConexion;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class PaymentForecastTrainCommand {

    private static final Path ARTIFACT_DIR = Path.of("var", "payment_forecast_ai");
    private static final Path DATASET_PATH = ARTIFACT_DIR.resolve("payment_forecast_dataset.csv");
    private static final Path TRAIN_SCRIPT = Path.of("src", "main", "python", "train_payment_forecast.py");

    public static void main(String[] args) {
        try {
            TrainingResult result = train();
            System.out.println("Dataset temporel genere: " + result.datasetPath().toAbsolutePath());
            System.out.println("Lignes exportees: " + result.exportedRows());
            System.out.println("Artefacts ML generes dans: " + result.artifactDir().toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Erreur entrainement prediction paiement: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public static TrainingResult train() throws SQLException, IOException, InterruptedException {
        MyConexion.initDatabase();
        Files.createDirectories(ARTIFACT_DIR);
        int rows = exportTemporalDataset(DATASET_PATH);
        runTrainingScript(DATASET_PATH, ARTIFACT_DIR);
        return new TrainingResult(DATASET_PATH, ARTIFACT_DIR, rows);
    }

    private static int exportTemporalDataset(Path datasetPath) throws SQLException, IOException {
        ServiceCommande serviceCommande = new ServiceCommande();
        ServicePayment servicePayment = new ServicePayment();
        List<Commande> commandes = serviceCommande.readAll();
        List<Payment> payments = servicePayment.readAll();

        Map<Integer, Payment> paymentByCommandeId = new HashMap<>();
        for (Payment payment : payments) {
            paymentByCommandeId.put(payment.getCommandeId(), payment);
        }

        Map<LocalDate, DailyTrainingRow> rowsByDate = new TreeMap<>();
        for (Commande commande : commandes) {
            LocalDate date = toLocalDate(commande.getDateCommande());
            if (date == null) {
                continue;
            }

            DailyTrainingRow row = rowsByDate.computeIfAbsent(date, ignored -> new DailyTrainingRow());
            row.totalOrders++;

            Payment payment = paymentByCommandeId.get(commande.getId());
            if (payment != null) {
                row.revenue += payment.getMontant();
                row.paidOrders++;
            } else if (isFailureStatus(commande.getStatut())) {
                row.failedOrders++;
            }
        }

        if (rowsByDate.isEmpty()) {
            rowsByDate.put(LocalDate.now(), new DailyTrainingRow());
        } else {
            fillMissingDays(rowsByDate);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(datasetPath, StandardCharsets.UTF_8)) {
            writer.write("date,revenue,paid_orders,failed_orders,total_orders");
            writer.newLine();
            for (Map.Entry<LocalDate, DailyTrainingRow> entry : rowsByDate.entrySet()) {
                DailyTrainingRow row = entry.getValue();
                writer.write(String.format(
                        Locale.US,
                        "%s,%.2f,%d,%d,%d",
                        entry.getKey(),
                        row.revenue,
                        row.paidOrders,
                        row.failedOrders,
                        row.totalOrders
                ));
                writer.newLine();
            }
        }

        return rowsByDate.size();
    }

    private static void runTrainingScript(Path datasetPath, Path artifactDir) throws IOException, InterruptedException {
        String pythonCommand = System.getProperty("python.command", "python");
        ProcessBuilder builder = new ProcessBuilder(
                pythonCommand,
                TRAIN_SCRIPT.toString(),
                "--dataset", datasetPath.toString(),
                "--artifact-dir", artifactDir.toString()
        );
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if (!output.isBlank()) {
            System.out.println(output.trim());
        }
        if (exitCode != 0) {
            throw new IllegalStateException("Le script Python a echoue avec le code " + exitCode);
        }
    }

    private static void fillMissingDays(Map<LocalDate, DailyTrainingRow> rowsByDate) {
        LocalDate start = rowsByDate.keySet().stream().min(Comparator.naturalOrder()).orElse(LocalDate.now());
        LocalDate end = LocalDate.now();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            rowsByDate.computeIfAbsent(cursor, ignored -> new DailyTrainingRow());
            cursor = cursor.plusDays(1);
        }
    }

    private static boolean isFailureStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return normalized.contains("ANNULEE")
                || normalized.contains("ECHEC")
                || normalized.contains("FAILED")
                || normalized.contains("CANCEL");
    }

    private static LocalDate toLocalDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return new java.sql.Date(date.getTime()).toLocalDate();
    }

    private static final class DailyTrainingRow {
        private double revenue;
        private int paidOrders;
        private int failedOrders;
        private int totalOrders;
    }

    public record TrainingResult(Path datasetPath, Path artifactDir, int exportedRows) {
    }
}

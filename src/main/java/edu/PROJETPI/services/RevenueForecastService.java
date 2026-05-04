package edu.PROJETPI.services;

import edu.PROJETPI.AdminDashboardController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RevenueForecastService {

    private static final DecimalFormat MONEY_FORMAT =
            new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE));
    private static final WeekFields WEEK_FIELDS = WeekFields.ISO;
    private static final Path METADATA_PATH = Path.of("var", "payment_forecast_ai", "payment_forecast_metadata.json");

    public PredictionSnapshot analyze(List<AdminDashboardController.PaymentRow> paymentRows) {
        List<AdminDashboardController.PaymentRow> validRows = paymentRows == null ? List.of() : paymentRows.stream()
                .filter(row -> row.getActualDate() != null)
                .sorted(Comparator.comparing(AdminDashboardController.PaymentRow::getActualDate))
                .toList();

        Map<LocalDate, DailyStats> dailySeries = buildDailySeries(validRows);
        List<DailyPoint> last14Days = buildLast14Days(dailySeries);
        List<WeeklyPoint> last8Weeks = buildLast8Weeks(dailySeries);
        List<DistributionPoint> weekDistribution = buildWeekDistribution(validRows);

        ForecastValues forecast = loadMlForecast().orElseGet(() -> buildFallbackForecast(dailySeries));

        double totalRevenue = validRows.stream().mapToDouble(AdminDashboardController.PaymentRow::getMontant).sum();
        int totalOrders = validRows.size();
        double averageTicket = totalOrders == 0 ? 0 : totalRevenue / totalOrders;

        double todayRevenue = sumRevenueBetween(validRows, LocalDate.now(), LocalDate.now());
        double last7Revenue = sumRevenueBetween(validRows, LocalDate.now().minusDays(6), LocalDate.now());
        double last30Revenue = sumRevenueBetween(validRows, LocalDate.now().minusDays(29), LocalDate.now());
        double trend7Days = computeTrendPercentage(validRows, 7);

        return new PredictionSnapshot(
                forecast.predictedNextDayRevenue(),
                forecast.predictedWeekRevenue(),
                forecast.predictedMonthRevenue(),
                forecast.predictedNextDayOrders(),
                forecast.predictedWeekOrders(),
                forecast.predictedMonthOrders(),
                averageTicket,
                todayRevenue,
                last7Revenue,
                last30Revenue,
                totalOrders,
                trend7Days,
                forecast.sourceLabel(),
                forecast.trainedAt(),
                last14Days,
                last8Weeks,
                weekDistribution
        );
    }

    private ForecastValues buildFallbackForecast(Map<LocalDate, DailyStats> dailySeries) {
        double predictedNextDayRevenue = predictRevenueForDate(dailySeries, LocalDate.now().plusDays(1));
        int predictedNextDayOrders = predictOrdersForDate(dailySeries, LocalDate.now().plusDays(1));

        double predictedWeekRevenue = 0;
        double predictedMonthRevenue = 0;
        int predictedWeekOrders = 0;
        int predictedMonthOrders = 0;

        for (int i = 1; i <= 7; i++) {
            LocalDate targetDate = LocalDate.now().plusDays(i);
            predictedWeekRevenue += predictRevenueForDate(dailySeries, targetDate);
            predictedWeekOrders += predictOrdersForDate(dailySeries, targetDate);
        }

        for (int i = 1; i <= 30; i++) {
            LocalDate targetDate = LocalDate.now().plusDays(i);
            predictedMonthRevenue += predictRevenueForDate(dailySeries, targetDate);
            predictedMonthOrders += predictOrdersForDate(dailySeries, targetDate);
        }

        return new ForecastValues(
                predictedNextDayRevenue,
                predictedWeekRevenue,
                predictedMonthRevenue,
                predictedNextDayOrders,
                predictedWeekOrders,
                predictedMonthOrders,
                "Fallback",
                ""
        );
    }

    private Optional<ForecastValues> loadMlForecast() {
        if (!Files.exists(METADATA_PATH)) {
            return Optional.empty();
        }

        try {
            String json = Files.readString(METADATA_PATH, StandardCharsets.UTF_8);
            boolean trained = readBoolean(json, "trained").orElse(false);
            if (!trained) {
                return Optional.empty();
            }

            return Optional.of(new ForecastValues(
                    readDouble(json, "next_day_revenue").orElse(0.0),
                    readDouble(json, "next_7_days_revenue").orElse(0.0),
                    readDouble(json, "next_30_days_revenue").orElse(0.0),
                    readInt(json, "next_day_orders").orElse(0),
                    readInt(json, "next_7_days_orders").orElse(0),
                    readInt(json, "next_30_days_orders").orElse(0),
                    "ML entraine",
                    readString(json, "trained_at").orElse("")
            ));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<Double> readDouble(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
                .matcher(json);
        return matcher.find() ? Optional.of(Double.parseDouble(matcher.group(1))) : Optional.empty();
    }

    private Optional<Integer> readInt(String json, String key) {
        return readDouble(json, key).map(value -> (int) Math.round(value));
    }

    private Optional<Boolean> readBoolean(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)")
                .matcher(json);
        return matcher.find() ? Optional.of(Boolean.parseBoolean(matcher.group(1))) : Optional.empty();
    }

    private Optional<String> readString(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(json);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private Map<LocalDate, DailyStats> buildDailySeries(List<AdminDashboardController.PaymentRow> validRows) {
        Map<LocalDate, DailyStats> grouped = new TreeMap<>();
        for (AdminDashboardController.PaymentRow row : validRows) {
            grouped.computeIfAbsent(row.getActualDate(), ignored -> new DailyStats()).add(row.getMontant());
        }

        if (grouped.isEmpty()) {
            grouped.put(LocalDate.now(), new DailyStats());
            return grouped;
        }

        LocalDate start = grouped.keySet().stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate lastPaymentDate = grouped.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate end = LocalDate.now().isAfter(lastPaymentDate) ? LocalDate.now() : lastPaymentDate;

        Map<LocalDate, DailyStats> completed = new TreeMap<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            completed.put(cursor, grouped.getOrDefault(cursor, new DailyStats()));
            cursor = cursor.plusDays(1);
        }
        return completed;
    }

    private List<DailyPoint> buildLast14Days(Map<LocalDate, DailyStats> dailySeries) {
        List<DailyPoint> result = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(13);
        for (int i = 0; i < 14; i++) {
            LocalDate date = start.plusDays(i);
            DailyStats stats = dailySeries.getOrDefault(date, new DailyStats());
            result.add(new DailyPoint(date, stats.revenue, stats.count));
        }
        return result;
    }

    private List<WeeklyPoint> buildLast8Weeks(Map<LocalDate, DailyStats> dailySeries) {
        Map<String, WeeklyStats> grouped = new TreeMap<>();
        LocalDate start = LocalDate.now().minusWeeks(7).with(DayOfWeek.MONDAY);
        LocalDate end = LocalDate.now();

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            DailyStats stats = dailySeries.getOrDefault(cursor, new DailyStats());
            int week = cursor.get(WEEK_FIELDS.weekOfWeekBasedYear());
            int year = cursor.get(WEEK_FIELDS.weekBasedYear());
            String key = String.format("S%02d %d", week, year);
            grouped.computeIfAbsent(key, ignored -> new WeeklyStats()).add(stats.revenue, stats.count);
            cursor = cursor.plusDays(1);
        }

        return grouped.entrySet().stream()
                .map(entry -> new WeeklyPoint(entry.getKey(), entry.getValue().revenue, entry.getValue().count))
                .toList();
    }

    private List<DistributionPoint> buildWeekDistribution(List<AdminDashboardController.PaymentRow> validRows) {
        Map<DayOfWeek, WeeklyStats> grouped = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            grouped.put(dayOfWeek, new WeeklyStats());
        }

        for (AdminDashboardController.PaymentRow row : validRows) {
            grouped.get(row.getActualDate().getDayOfWeek()).add(row.getMontant(), 1);
        }

        List<DistributionPoint> result = new ArrayList<>();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            WeeklyStats stats = grouped.get(dayOfWeek);
            result.add(new DistributionPoint(labelForDay(dayOfWeek), stats.revenue, stats.count));
        }
        return result;
    }

    private double predictRevenueForDate(Map<LocalDate, DailyStats> dailySeries, LocalDate targetDate) {
        double recentAverage = averageDouble(extractRecentRevenues(dailySeries, 7));
        double weekdayAverage = averageForWeekdayRevenue(dailySeries, targetDate.getDayOfWeek());
        double previousAverage = averageDouble(extractRecentRevenues(dailySeries, 14).stream().limit(7).toList());
        double trendFactor = previousAverage <= 0 ? 1.0 : clamp(recentAverage / previousAverage, 0.75, 1.30);

        if (recentAverage <= 0 && weekdayAverage <= 0) {
            return 0;
        }

        double baseline = (recentAverage * 0.65) + (weekdayAverage * 0.35);
        return Math.max(0, baseline * trendFactor);
    }

    private int predictOrdersForDate(Map<LocalDate, DailyStats> dailySeries, LocalDate targetDate) {
        double recentAverage = averageInt(extractRecentCounts(dailySeries, 7));
        double weekdayAverage = averageForWeekdayCount(dailySeries, targetDate.getDayOfWeek());
        double previousAverage = averageInt(extractRecentCounts(dailySeries, 14).stream().limit(7).toList());
        double trendFactor = previousAverage <= 0 ? 1.0 : clamp(recentAverage / previousAverage, 0.75, 1.30);

        if (recentAverage <= 0 && weekdayAverage <= 0) {
            return 0;
        }

        return Math.max(0, (int) Math.round(((recentAverage * 0.65) + (weekdayAverage * 0.35)) * trendFactor));
    }

    private List<Double> extractRecentRevenues(Map<LocalDate, DailyStats> dailySeries, int days) {
        List<Double> values = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        for (int i = 0; i < days; i++) {
            values.add(dailySeries.getOrDefault(start.plusDays(i), new DailyStats()).revenue);
        }
        return values;
    }

    private List<Integer> extractRecentCounts(Map<LocalDate, DailyStats> dailySeries, int days) {
        List<Integer> values = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        for (int i = 0; i < days; i++) {
            values.add(dailySeries.getOrDefault(start.plusDays(i), new DailyStats()).count);
        }
        return values;
    }

    private double averageForWeekdayRevenue(Map<LocalDate, DailyStats> dailySeries, DayOfWeek targetDay) {
        return dailySeries.entrySet().stream()
                .filter(entry -> entry.getKey().getDayOfWeek() == targetDay)
                .mapToDouble(entry -> entry.getValue().revenue)
                .average()
                .orElse(0);
    }

    private double averageForWeekdayCount(Map<LocalDate, DailyStats> dailySeries, DayOfWeek targetDay) {
        return dailySeries.entrySet().stream()
                .filter(entry -> entry.getKey().getDayOfWeek() == targetDay)
                .mapToInt(entry -> entry.getValue().count)
                .average()
                .orElse(0);
    }

    private double sumRevenueBetween(List<AdminDashboardController.PaymentRow> rows, LocalDate start, LocalDate end) {
        return rows.stream()
                .filter(row -> !row.getActualDate().isBefore(start) && !row.getActualDate().isAfter(end))
                .mapToDouble(AdminDashboardController.PaymentRow::getMontant)
                .sum();
    }

    private double computeTrendPercentage(List<AdminDashboardController.PaymentRow> rows, int days) {
        double current = sumRevenueBetween(rows, LocalDate.now().minusDays(days - 1L), LocalDate.now());
        double previous = sumRevenueBetween(rows, LocalDate.now().minusDays((days * 2L) - 1L), LocalDate.now().minusDays(days));
        if (previous <= 0) {
            return current > 0 ? 100 : 0;
        }
        return ((current - previous) / previous) * 100;
    }

    private double averageDouble(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double averageInt(List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String labelForDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Lun";
            case TUESDAY -> "Mar";
            case WEDNESDAY -> "Mer";
            case THURSDAY -> "Jeu";
            case FRIDAY -> "Ven";
            case SATURDAY -> "Sam";
            case SUNDAY -> "Dim";
        };
    }

    public String formatMoney(double amount) {
        return MONEY_FORMAT.format(amount) + " TND";
    }

    public String formatTrend(double trend) {
        return String.format(Locale.US, "%+.2f%%", trend);
    }

    public record PredictionSnapshot(
            double predictedNextDayRevenue,
            double predictedWeekRevenue,
            double predictedMonthRevenue,
            int predictedNextDayOrders,
            int predictedWeekOrders,
            int predictedMonthOrders,
            double averageTicket,
            double todayRevenue,
            double last7DaysRevenue,
            double last30DaysRevenue,
            int totalPaidPayments,
            double trend7Days,
            String sourceLabel,
            String trainedAt,
            List<DailyPoint> dailyPoints,
            List<WeeklyPoint> weeklyPoints,
            List<DistributionPoint> distributionPoints
    ) {
    }

    public record DailyPoint(LocalDate date, double revenue, int count) {
    }

    public record WeeklyPoint(String label, double revenue, int count) {
    }

    public record DistributionPoint(String label, double revenue, int count) {
    }

    private record ForecastValues(
            double predictedNextDayRevenue,
            double predictedWeekRevenue,
            double predictedMonthRevenue,
            int predictedNextDayOrders,
            int predictedWeekOrders,
            int predictedMonthOrders,
            String sourceLabel,
            String trainedAt
    ) {
    }

    private static final class DailyStats {
        private double revenue;
        private int count;

        private void add(double montant) {
            revenue += montant;
            count++;
        }
    }

    private static final class WeeklyStats {
        private double revenue;
        private int count;

        private void add(double montant, int quantity) {
            revenue += montant;
            count += quantity;
        }
    }
}

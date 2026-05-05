package edu.esportify.controllers;

import java.util.List;

final class FormOptions {
    static final List<String> RANKS = List.of(
            "Debutant",
            "Amateur",
            "Bronze",
            "Silver",
            "Gold",
            "Diamenet",
            "Heroique",
            "Legend",
            "Grandmaster"
    );

    static final List<String> REGIONS = List.of(
            "Afrique",
            "Amerique",
            "Asie",
            "Europe",
            "Oceanie",
            "Antarctique"
    );
    static final List<String> AVAILABILITIES = List.of("Soir", "Toute la journee", "Jours weekend");

    private FormOptions() {
    }

    static String fallbackRank(String value) {
        return containsIgnoreCase(RANKS, value) ? findCanonical(RANKS, value) : "Debutant";
    }

    static String fallbackRegion(String value) {
        return containsIgnoreCase(REGIONS, value) ? findCanonical(REGIONS, value) : "Europe";
    }

    static String fallbackAvailability(String value) {
        return containsIgnoreCase(AVAILABILITIES, value) ? findCanonical(AVAILABILITIES, value) : "Soir";
    }

    private static boolean containsIgnoreCase(List<String> values, String value) {
        if (value == null) {
            return false;
        }
        return values.stream().anyMatch(item -> item.equalsIgnoreCase(value.trim()));
    }

    private static String findCanonical(List<String> values, String value) {
        String normalized = value == null ? "" : value.trim();
        return values.stream()
                .filter(item -> item.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(values.get(0));
    }
}

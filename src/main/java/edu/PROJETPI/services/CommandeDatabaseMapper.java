package edu.PROJETPI.services;

import edu.PROJETPI.entites.Commande;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

final class CommandeDatabaseMapper {

    private static final String IDENTITY_PREFIX = "PI_CMD|";
    private static final String DELIVERY_ADDRESS_PREFIX = "ADR:";
    private static final String DELIVERY_DESCRIPTION_PREFIX = "DESC:";

    private CommandeDatabaseMapper() {
    }

    static String buildIdentityKey(Commande commande, double total) {
        if (commande == null || commande.getDateCommande() == null) {
            return null;
        }

        LocalDate localDate = new java.sql.Date(commande.getDateCommande().getTime()).toLocalDate();
        return IDENTITY_PREFIX + localDate + "|" + String.format(Locale.US, "%.2f", total);
    }

    static Date extractCommandeDate(ResultSet rs) throws SQLException {
        Timestamp paymentDate = rs.getTimestamp("payment_date_calc");
        if (paymentDate != null) {
            return new Date(paymentDate.getTime());
        }

        String identityKey = rs.getString("identity_key");
        if (identityKey == null || !identityKey.startsWith(IDENTITY_PREFIX)) {
            return null;
        }

        String[] parts = identityKey.split("\\|", -1);
        if (parts.length < 3) {
            return null;
        }

        try {
            LocalDate localDate = LocalDate.parse(parts[1]);
            return java.sql.Date.valueOf(localDate);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    static double extractCommandeTotal(ResultSet rs) throws SQLException {
        double totalFromLines = rs.getDouble("total_calc");
        if (!rs.wasNull() && totalFromLines > 0) {
            return totalFromLines;
        }

        String identityKey = rs.getString("identity_key");
        if (identityKey == null || !identityKey.startsWith(IDENTITY_PREFIX)) {
            return totalFromLines;
        }

        String[] parts = identityKey.split("\\|", -1);
        if (parts.length < 3) {
            return totalFromLines;
        }

        try {
            return Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            return totalFromLines;
        }
    }

    static String buildAdresseDetail(Commande commande) {
        String adresseLivraison = normalize(commande.getAdresseLivraison());
        String description = normalize(commande.getDescriptionLivraison());

        if (adresseLivraison == null && description == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        if (adresseLivraison != null) {
            builder.append(DELIVERY_ADDRESS_PREFIX).append(adresseLivraison);
        }
        if (description != null) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(DELIVERY_DESCRIPTION_PREFIX).append(description);
        }
        return builder.toString();
    }

    static void populateDeliveryFields(Commande commande, String adresseDetail) {
        if (commande == null || adresseDetail == null || adresseDetail.isBlank()) {
            return;
        }

        String[] lines = adresseDetail.split("\\R");
        boolean structured = false;
        StringBuilder descriptionBuilder = new StringBuilder();
        for (String rawLine : lines) {
            String line = rawLine == null ? "" : rawLine.trim();
            if (line.startsWith(DELIVERY_ADDRESS_PREFIX)) {
                commande.setAdresseLivraison(blankToNull(line.substring(DELIVERY_ADDRESS_PREFIX.length()).trim()));
                structured = true;
                continue;
            }
            if (line.startsWith(DELIVERY_DESCRIPTION_PREFIX)) {
                String value = line.substring(DELIVERY_DESCRIPTION_PREFIX.length()).trim();
                if (!value.isBlank()) {
                    if (descriptionBuilder.length() > 0) {
                        descriptionBuilder.append('\n');
                    }
                    descriptionBuilder.append(value);
                }
                structured = true;
            }
        }

        if (descriptionBuilder.length() > 0) {
            commande.setDescriptionLivraison(descriptionBuilder.toString());
        }

        if (!structured) {
            commande.setDescriptionLivraison(blankToNull(adresseDetail));
        }
    }

    static Integer toDatabaseTelephone(String telephone) {
        String normalized = normalize(telephone);
        if (normalized == null) {
            return null;
        }

        try {
            return Integer.valueOf(normalized.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String fromDatabaseTelephone(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return value == null ? null : String.valueOf(value);
    }

    static Integer normalizeUserId(int clientId) {
        return clientId > 0 ? clientId : null;
    }

    static int extractClientId(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        return rs.wasNull() ? 0 : userId;
    }

    static int resolveCommandeQuantite(Commande commande, int fallbackQuantite) {
        return Math.max(fallbackQuantite, 0);
    }

    private static String normalize(String value) {
        return blankToNull(value == null ? null : value.trim());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

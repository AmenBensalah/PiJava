package edu.PROJETPI.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class StripeConfig {

    private static final String DEFAULT_SUCCESS_URL = "https://example.com/success?session_id={CHECKOUT_SESSION_ID}";
    private static final String DEFAULT_CANCEL_URL = "https://example.com/cancel";
    private static final String DEFAULT_CURRENCY = "eur";
    private static final Properties PROPERTIES = loadProperties();

    private StripeConfig() {
    }

    public static String getSecretKey() {
        return firstNonBlank(
                System.getenv("STRIPE_SECRET_KEY"),
                System.getProperty("stripe.secret.key"),
                PROPERTIES.getProperty("stripe.secret.key")
        );
    }

    public static String getSuccessUrl() {
        return firstNonBlank(
                System.getenv("STRIPE_SUCCESS_URL"),
                System.getProperty("stripe.success.url"),
                PROPERTIES.getProperty("stripe.success.url"),
                DEFAULT_SUCCESS_URL
        );
    }

    public static String getCancelUrl() {
        return firstNonBlank(
                System.getenv("STRIPE_CANCEL_URL"),
                System.getProperty("stripe.cancel.url"),
                PROPERTIES.getProperty("stripe.cancel.url"),
                DEFAULT_CANCEL_URL
        );
    }

    public static String getCurrency() {
        return firstNonBlank(
                System.getenv("STRIPE_CURRENCY"),
                System.getProperty("stripe.currency"),
                PROPERTIES.getProperty("stripe.currency"),
                DEFAULT_CURRENCY
        );
    }

    public static boolean isConfigured() {
        String key = getSecretKey();
        return key != null && !key.isBlank() && !key.contains("YOUR_STRIPE_SECRET_KEY");
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = StripeConfig.class.getResourceAsStream("/stripe.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
        }
        return properties;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

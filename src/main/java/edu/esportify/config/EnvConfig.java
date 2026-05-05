package edu.esportify.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EnvConfig {
    private static final Map<String, String> DOTENV_VALUES = loadDotEnv();

    private EnvConfig() {
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String envValue = trimToNull(System.getenv(key));
        if (envValue != null) {
            return envValue;
        }

        String systemValue = trimToNull(System.getProperty(key));
        if (systemValue != null) {
            return systemValue;
        }

        String dotenvValue = trimToNull(DOTENV_VALUES.get(key));
        if (dotenvValue != null) {
            return dotenvValue;
        }

        return defaultValue;
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new LinkedHashMap<>();
        loadFromFile(Path.of(".env"), values);
        loadFromFile(Path.of("PiJava", ".env"), values);
        return values;
    }

    private static void loadFromFile(Path path, Map<String, String> values) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String rawLine : lines) {
                if (rawLine == null) {
                    continue;
                }
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx <= 0) {
                    continue;
                }
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if (value.length() >= 2) {
                    boolean quoted = (value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'"));
                    if (quoted) {
                        value = value.substring(1, value.length() - 1);
                    }
                }
                if (!key.isBlank() && !values.containsKey(key)) {
                    values.put(key, value);
                }
            }
        } catch (IOException ignored) {
            // Ignore malformed or missing .env files.
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

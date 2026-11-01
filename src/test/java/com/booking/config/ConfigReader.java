package com.booking.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
    private static final String CONFIG_FILE = "/config.properties";
    private static final String BASE_URL_KEY = "base.url";
    private static final Properties PROPERTIES = loadProperties();

    private ConfigReader() {
    }

    public static String getBaseUrl() {
        return getProperty(BASE_URL_KEY).replaceAll("/+$", "");
    }

    public static String getProperty(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Configuration property key must not be blank");
        }

        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = PROPERTIES.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration property: " + key);
        }

        return value.trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = ConfigReader.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found: " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read configuration file: " + CONFIG_FILE, e);
        }

        return properties;
    }
}

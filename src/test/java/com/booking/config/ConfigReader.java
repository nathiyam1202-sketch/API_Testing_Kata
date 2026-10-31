package com.booking.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
    private static final String CONFIG_FILE = "/config.properties";
    private static final String BASE_URL_KEY = "base.url";
    private static final String BASE_URL;

    static {
        BASE_URL = loadBaseUrl();
    }

    private ConfigReader() {
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    private static String loadBaseUrl() {
        Properties properties = new Properties();
        try (InputStream input = ConfigReader.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found: " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read configuration file: " + CONFIG_FILE, e);
        }

        String baseUrl = System.getProperty(BASE_URL_KEY);
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = properties.getProperty(BASE_URL_KEY);
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Missing required configuration property: " + BASE_URL_KEY);
        }

        return baseUrl.trim().replaceAll("/+$", "");
    }
}

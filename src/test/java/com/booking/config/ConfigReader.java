package com.booking.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public final class ConfigReader {
  private static final String CONFIG_FILE = "/config.properties";
  private static final String CONFIG_FILE_LOCATION = "src/test/resources/config.properties";
  private static final String BASE_URL_KEY = "base.url";
  private static final String AUTH_USERNAME_KEY = "auth.username";
  private static final String AUTH_PASSWORD_KEY = "auth.password";
  private static final Properties PROPERTIES = loadProperties();

  private ConfigReader() {}

  public static String getBaseUrl() {
    return getProperty(BASE_URL_KEY).replaceAll("/+$", "");
  }

  public static String getAuthUsername() {
    return getProperty(AUTH_USERNAME_KEY);
  }

  public static String getAuthPassword() {
    return getProperty(AUTH_PASSWORD_KEY);
  }

  public static String getProperty(String key) {
    validateKey(key);
    String value = System.getProperty(key);
    if (!isConfigured(value)) {
      value = System.getenv(environmentKey(key));
    }
    if (!isConfigured(value)) {
      value = PROPERTIES.getProperty(key);
    }
    if (!isConfigured(value)) {
      throw new IllegalStateException(
          "Missing required configuration property '"
              + key
              + "'. Set it in "
              + CONFIG_FILE_LOCATION
              + ", as environment variable "
              + environmentKey(key)
              + ", or as JVM property -D"
              + key
              + ".");
    }
    return value.trim();
  }

  private static String environmentKey(String key) {
    return key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
  }

  private static Properties loadProperties() {
    Properties properties = new Properties();
    try (InputStream input = ConfigReader.class.getResourceAsStream(CONFIG_FILE)) {
      if (input == null) {
        return properties;
      }
      properties.load(input);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Unable to read configuration file at " + CONFIG_FILE_LOCATION + ".", e);
    }
    return properties;
  }

  private static boolean isConfigured(String value) {
    return value != null && !value.isBlank();
  }

  private static void validateKey(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Configuration property key must not be blank");
    }
  }
}

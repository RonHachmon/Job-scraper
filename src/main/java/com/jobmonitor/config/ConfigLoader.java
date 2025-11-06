package com.jobmonitor.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigLoader {
    private static final String CONFIG_FILE = "application.properties";

    public static AppConfig loadConfig() {
        Properties props = loadProperties();
        
        return new AppConfig.Builder()
                .jobsFile(getProperty(props, "jobs.file", "jobs.txt"))
                .snippetMaxLength(getIntProperty(props, "snippet.max.length", 80))
                .checkIntervalMinutes(getIntProperty(props, "check.interval.minutes", 60))
                .sleepHour(getIntProperty(props, "sleep.hour", 23))
                .sleepTimeHours(getIntProperty(props, "sleep.time.hours", 7))
                .countryCode(getProperty(props, "google.country.code", "IL"))
                .positions(parseList(props, "search.positions"))
                .levels(parseList(props, "search.levels"))
                .locations(parseList(props, "search.locations"))
                .excludedPageTerms(parseList(props, "excluded.page.terms"))
                .excludedTitleTerms(parseList(props, "excluded.title.terms"))
                .telegramBotToken(System.getenv("TELEGRAM_BOT_TOKEN"))
                .telegramChatId(System.getenv("TELEGRAM_CHAT_ID"))
                .apiKey(System.getenv("API_KEY"))
                .searchEngineId(System.getenv("CX"))
                .build();
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Unable to find " + CONFIG_FILE);
                return props;
            }
            props.load(input);
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
        return props;
    }

    private static String getProperty(Properties props, String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    private static int getIntProperty(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }


    private static List<String> parseList(Properties props, String key) {
        String value = props.getProperty(key, "");
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

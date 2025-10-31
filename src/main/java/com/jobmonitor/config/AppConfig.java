package com.jobmonitor.config;

import java.util.List;

public class AppConfig {
    private final String jobsFile;
    private final int snippetMaxLength;
    private final int checkIntervalMinutes;
    private final int sleepHour;
    private final long sleepTimeMs;
    
    private final String telegramBotToken;
    private final String telegramChatId;
    
    private final String apiKey;
    private final String searchEngineId;
    private final String countryCode;
    
    private final List<String> positions;
    private final List<String> levels;
    private final List<String> locations;
    private final List<String> excludedPageTerms;
    private final List<String> excludedTitleTerms;

    private AppConfig(Builder builder) {
        this.jobsFile = builder.jobsFile;
        this.snippetMaxLength = builder.snippetMaxLength;
        this.checkIntervalMinutes = builder.checkIntervalMinutes;
        this.sleepHour = builder.sleepHour;
        this.sleepTimeMs = builder.sleepTimeHour;
        this.telegramBotToken = builder.telegramBotToken;
        this.telegramChatId = builder.telegramChatId;
        this.apiKey = builder.apiKey;
        this.searchEngineId = builder.searchEngineId;
        this.countryCode = builder.countryCode;
        this.positions = builder.positions;
        this.levels = builder.levels;
        this.locations = builder.locations;
        this.excludedPageTerms = builder.excludedPageTerms;
        this.excludedTitleTerms = builder.excludedTitleTerms;
    }

    public String getJobsFile() { return jobsFile; }
    public int getSnippetMaxLength() { return snippetMaxLength; }
    public int getCheckIntervalMinutes() { return checkIntervalMinutes; }
    public long getCheckIntervalMs() { return checkIntervalMinutes * 60L * 1000L; }
    public int getSleepHour() { return sleepHour; }
    public long getSleepTimeMs() { return sleepTimeMs; }
    public String getTelegramBotToken() { return telegramBotToken; }
    public String getTelegramChatId() { return telegramChatId; }
    public String getApiKey() { return apiKey; }
    public String getSearchEngineId() { return searchEngineId; }
    public String getCountryCode() { return countryCode; }
    public List<String> getPositions() { return positions; }
    public List<String> getLevels() { return levels; }
    public List<String> getLocations() { return locations; }
    public List<String> getExcludedPageTerms() { return excludedPageTerms; }
    public List<String> getExcludedTitleTerms() { return excludedTitleTerms; }

    public static class Builder {
        private String jobsFile = "jobs.json";
        private int snippetMaxLength = 80;
        private int checkIntervalMinutes = 60;
        private int sleepHour = 23;

        //Time in sleep, until resume work
        private long sleepTimeHour = 7;
        private String telegramBotToken;
        private String telegramChatId;
        private String apiKey;
        private String searchEngineId;
        private String countryCode = "IL";
        private List<String> positions;
        private List<String> levels;
        private List<String> locations;
        private List<String> excludedPageTerms;
        private List<String> excludedTitleTerms;

        public Builder jobsFile(String jobsFile) {
            this.jobsFile = jobsFile;
            return this;
        }

        public Builder snippetMaxLength(int snippetMaxLength) {
            this.snippetMaxLength = snippetMaxLength;
            return this;
        }

        public Builder checkIntervalMinutes(int checkIntervalMinutes) {
            this.checkIntervalMinutes = checkIntervalMinutes;
            return this;
        }

        public Builder sleepHour(int sleepHour) {
            this.sleepHour = sleepHour;
            return this;
        }

        public Builder telegramBotToken(String telegramBotToken) {
            this.telegramBotToken = telegramBotToken;
            return this;
        }

        public Builder telegramChatId(String telegramChatId) {
            this.telegramChatId = telegramChatId;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder searchEngineId(String searchEngineId) {
            this.searchEngineId = searchEngineId;
            return this;
        }

        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder positions(List<String> positions) {
            this.positions = positions;
            return this;
        }

        public Builder levels(List<String> levels) {
            this.levels = levels;
            return this;
        }

        public Builder locations(List<String> locations) {
            this.locations = locations;
            return this;
        }

        public Builder excludedPageTerms(List<String> excludedPageTerms) {
            this.excludedPageTerms = excludedPageTerms;
            return this;
        }

        public Builder excludedTitleTerms(List<String> excludedTitleTerms) {
            this.excludedTitleTerms = excludedTitleTerms;
            return this;
        }

        public AppConfig build() {
            return new AppConfig(this);
        }

        public Builder sleepTimeHours(int hours) {
            this.sleepTimeHour = hours;
            return this;
        }
    }
}

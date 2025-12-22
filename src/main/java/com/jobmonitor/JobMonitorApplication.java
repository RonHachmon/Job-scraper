package com.jobmonitor;

import com.jobmonitor.config.AppConfig;
import com.jobmonitor.config.ConfigLoader;
import com.jobmonitor.model.Job;
import com.jobmonitor.notifier.ConsoleNotifier;
import com.jobmonitor.notifier.Notifier;
import com.jobmonitor.notifier.TelegramNotifier;
import com.jobmonitor.service.GoogleSearchService;
import com.jobmonitor.service.JobFilter;
import com.jobmonitor.service.JobMonitorService;
import com.jobmonitor.service.JobsProvider;
import com.jobmonitor.service.scrapers.*;
import com.jobmonitor.storage.FileJobStorage;
import com.jobmonitor.storage.JobStorage;
import java.util.ArrayList;
import java.util.List;



public class JobMonitorApplication {







    public static void main(String[] args) throws Exception {

        if (System.getenv("DEV_ENV") != null) {
            scraperTest();
        }
        else {
            runJobScraper();
        }


    }

    private static void scraperTest() throws Exception {
        AppConfig config = ConfigLoader.loadConfig();
        JobFilter jobFilter = new JobFilter(config);
        //JobsProvider scraper = new NvidiaScraper(jobFilter);

        JobsProvider scraper = new AppleScraper(jobFilter);

        List<Job> jobs = scraper.fetchJobs();

        for (Job job:jobs){
            System.out.println(job);
        }
    }

    private static void runJobScraper() {
        AppConfig config = ConfigLoader.loadConfig();
        JobFilter jobFilter = new JobFilter(config);


        JobStorage storage = new FileJobStorage(config.getJobsFile());

        List<Notifier> notifiers = createNotifiers(config);

        List<JobsProvider> providers = createProviders(config, jobFilter);

        JobMonitorService monitorService = new JobMonitorService(
                config,
                providers,
                jobFilter,
                storage,
                notifiers
        );


        monitorService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(monitorService::stop));
    }

    private static List<JobsProvider> createProviders(AppConfig config, JobFilter jobFilter) {
        List<JobsProvider> providers = new ArrayList<>();


        System.out.println("DEBUG: Building Google service provider");
        JobsProvider searchService = new GoogleSearchService(config, jobFilter);
        providers.add(searchService);

        System.out.println("DEBUG: Imperva service provider");
        JobsProvider impervaScraper = new ImpervaScraper(jobFilter);
        providers.add(impervaScraper);

        System.out.println("DEBUG: Nvidia service provider");
        JobsProvider nvidiaScraper = new NvidiaScraper(jobFilter);
        providers.add(nvidiaScraper);


        System.out.println("DEBUG: Red Hat service provider");
        JobsProvider redHatScraper = new RedHatScraper(jobFilter);
        providers.add(redHatScraper);

        return providers;
    }


    private static void printConfiguration(AppConfig config) {
        System.out.println("=".repeat(80));
        System.out.println("Configuration Loaded:");
        System.out.println("=".repeat(80));
        System.out.println("Telegram Bot Token: " + maskSecret(config.getTelegramBotToken()));
        System.out.println("Telegram Chat ID: " + config.getTelegramChatId());
        System.out.println("Google API Key: " + maskSecret(config.getApiKey()));
        System.out.println("Search Engine ID: " + config.getSearchEngineId());
        System.out.println("Country Code: " + config.getCountryCode());
        System.out.println("Jobs File: " + config.getJobsFile());
        System.out.println("Check Interval: " + config.getCheckIntervalMinutes() + " minutes");
        System.out.println("=".repeat(80) + "\n");
    }

    private static String maskSecret(String secret) {
        if (secret == null || secret.isEmpty()) {
            return "[NOT SET]";
        }
        if (secret.length() <= 8) {
            return "****";
        }
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }

    public static void printAllEnvVars() {
        System.getenv().forEach((key, value) -> System.out.println(key + " = " + value));
    }

    private static List<Notifier> createNotifiers(AppConfig config) {
        List<Notifier> notifiers = new ArrayList<>();
        
        notifiers.add(new ConsoleNotifier());
        
        if (config.getTelegramBotToken() != null && !config.getTelegramBotToken().isEmpty()) {
            notifiers.add(new TelegramNotifier(
                    config.getTelegramBotToken(),
                    config.getTelegramChatId()
            ));
        }
        
        return notifiers;
    }
}

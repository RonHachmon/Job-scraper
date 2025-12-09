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
import com.jobmonitor.service.scrapers.ImpervaScraper;
import com.jobmonitor.service.scrapers.NvidiaScraper;
import com.jobmonitor.storage.FileJobStorage;
import com.jobmonitor.storage.JobStorage;
import java.util.ArrayList;
import java.util.List;



public class JobMonitorApplication {



//    public static void main(String[] args) throws Exception {
//        AppConfig config = ConfigLoader.loadConfig();
//        JobFilter jobFilter = new JobFilter(config);
//        NvidiaScraper nvidiaScraper = new NvidiaScraper(jobFilter);
//        List<Job> jobs = nvidiaScraper.fetchJobs();
//
//        for (Job job:jobs){
//            System.out.println(job);
//        }
//    }


    public static void main(String[] args) {
        AppConfig config = ConfigLoader.loadConfig();
        JobFilter jobFilter = new JobFilter(config);




        JobStorage storage = new FileJobStorage(config.getJobsFile());

        List<Notifier> notifiers = createNotifiers(config);
        List<JobsProvider> providers = new ArrayList<>();

        JobsProvider searchService = new GoogleSearchService(config,jobFilter);
        providers.add(searchService);

        JobsProvider impervaScraper = new ImpervaScraper(jobFilter);
        providers.add(impervaScraper);

        JobsProvider nvidiaScraper = new NvidiaScraper(jobFilter);
        providers.add(nvidiaScraper);

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

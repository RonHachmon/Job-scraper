package com.jobmonitor.service;

import com.jobmonitor.config.AppConfig;
import com.jobmonitor.model.Job;
import com.jobmonitor.notifier.Notifier;
import com.jobmonitor.storage.JobStorage;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JobMonitorService {
    private final AppConfig config;
    private final GoogleSearchService searchService;
    private final JobFilter jobFilter;
    private final JobStorage storage;
    private final List<Notifier> notifiers;
    private ScheduledExecutorService scheduler;

    public JobMonitorService(
            AppConfig config,
            GoogleSearchService searchService,
            JobFilter jobFilter,
            JobStorage storage,
            List<Notifier> notifiers) {
        this.config = config;
        this.searchService = searchService;
        this.jobFilter = jobFilter;
        this.storage = storage;
        this.notifiers = notifiers;
    }

    public void start() {
        System.out.println("Starting job monitoring...");
        System.out.println("Checking every " + config.getCheckIntervalMinutes() + " minutes\n");

        startScheduler();
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void startScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        long intervalMs = config.getCheckIntervalMs();
        
        scheduler.scheduleAtFixedRate(() -> {
            checkAndNotify();
            checkSleepTime();
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    private void checkAndNotify() {
        try {
            Set<String> storedLinks = storage.getStoredJobLinks();
            List<Job> currentJobs = searchService.fetchJobs();
            System.out.println("Fetched " + currentJobs.size() + " jobs from API");

            List<Job> filteredJobs = jobFilter.filterByTitle(currentJobs);
            System.out.println("Filtered " + filteredJobs.size() + " jobs after title filtering");

            List<Job> newJobs = jobFilter.filterNewJobs(filteredJobs, storedLinks);

            if (newJobs.isEmpty()) {
                System.out.println("No new jobs found");
                notifyNoNewJobs();
            } else {
                notifyNewJobs(newJobs);
                updateStorage(currentJobs);
            }
        } catch (Exception e) {
            System.err.println("Error checking jobs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void notifyNewJobs(List<Job> jobs) {
        for (Notifier notifier : notifiers) {
            try {
                notifier.notify(jobs);
            } catch (Exception e) {
                System.err.println("Error sending notification: " + e.getMessage());
            }
        }
        System.out.println("Sent notifications for " + jobs.size() + " new job(s)");
    }

    private void notifyNoNewJobs() {
        for (Notifier notifier : notifiers) {
            try {
                notifier.notify(List.of());
            } catch (Exception e) {
                System.err.println("Error sending notification: " + e.getMessage());
            }
        }
    }

    private void updateStorage(List<Job> jobs) {
        Set<String> links = jobs.stream()
                .map(Job::getLink)
                .collect(Collectors.toSet());
        storage.saveJobLinks(links);
    }

    private void checkSleepTime() {
        int currentHour = LocalTime.now().getHour();
        if (currentHour >= config.getSleepHour()) {
            System.out.println("Entering sleep mode for " + (config.getSleepTimeMs() / 3600000) + " hours...");
            stop();
            
            Executors.newSingleThreadScheduledExecutor().schedule(
                    this::start,
                    config.getSleepTimeMs(),
                    TimeUnit.HOURS
            );
        }
    }
}

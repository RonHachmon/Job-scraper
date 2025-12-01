package com.jobmonitor.service;

import com.jobmonitor.config.AppConfig;
import com.jobmonitor.model.Job;
import com.jobmonitor.notifier.Notifier;
import com.jobmonitor.storage.JobStorage;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JobMonitorService {
    private final AppConfig config;

    private final JobFilter jobFilter;
    private final JobStorage storage;
    private final List<Notifier> notifiers;
    private ScheduledExecutorService scheduler;

    private final List<JobsProvider> jobsProviders;

    private final Set<String> jobLinks;
    public JobMonitorService(
            AppConfig config,
            List<JobsProvider> jobsProviders,
            JobFilter jobFilter,
            JobStorage storage,
            List<Notifier> notifiers) {
        this.config = config;
        this.jobFilter = jobFilter;
        this.storage = storage;
        this.notifiers = notifiers;
        this.jobsProviders = jobsProviders;

        jobLinks = storage.getStoredJobLinks();
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
        long intervalMinutes = config.getCheckIntervalMinutes();
        
        scheduler.scheduleAtFixedRate(() -> {
            checkAndNotify();
            checkSleepTime();
        }, 0, intervalMinutes, TimeUnit.MINUTES);
    }

    private void checkAndNotify() {
        try {
            List<Job> currentJobs = new ArrayList<>();

            for(JobsProvider a: jobsProviders){
                currentJobs.addAll(a.fetchJobs());
            }


            System.out.println("Fetched " + currentJobs.size() + " jobs from providers");



            List<Job> newJobs = jobFilter.filterNewJobs(currentJobs, jobLinks);

            notifyJobs(newJobs);

            if(!newJobs.isEmpty()){

                updateNewLinks(newJobs);
            }
        } catch (Exception e) {
            System.err.println("Error checking jobs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void notifyJobs(List<Job>  jobs){
        for (Notifier notifier : notifiers) {
            try {
                notifier.notify(jobs);
            } catch (Exception e) {
                System.err.println("Error sending notification: " + e.getMessage());
            }
        }

        System.out.println("Sent notifications for " + jobs.size() + " new job(s)");

    }


    private void updateNewLinks(List<Job> jobs) {
        Set<String> links = jobs.stream()
                .map(Job::getLink)
                .collect(Collectors.toSet());


        jobLinks.addAll(links);
        storage.saveJobLinks(links);
    }

    private void checkSleepTime() {
        int currentHour = LocalTime.now().getHour();
        if (currentHour >= config.getSleepHour()) {

            stop();
            
            Executors.newSingleThreadScheduledExecutor().schedule(
                    this::start,
                    config.getSleepTimeMs(),
                    TimeUnit.HOURS
            );
        }
    }
}

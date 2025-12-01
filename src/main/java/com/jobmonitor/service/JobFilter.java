package com.jobmonitor.service;

import com.jobmonitor.config.AppConfig;
import com.jobmonitor.model.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JobFilter {

    private final AppConfig appConfig;
    private final List<String> excludedTitleTerms;

    public JobFilter(AppConfig appConfig) {
        this.appConfig = appConfig;

        this.excludedTitleTerms = new ArrayList<>(this.appConfig.getExcludedTitleTerms());

    }

    public List<Job> filterByTitle(List<Job> jobs) {
        return jobs.stream()
                .filter(job -> !containsExcludedTerm(job.getTitle()))
                .collect(Collectors.toList());
    }

    public List<Job> filterNewJobs(List<Job> newJobs, Set<String> oldJobsLinks) {
        if(oldJobsLinks.isEmpty()){
            return newJobs;
        }
        return newJobs.stream()
                .filter(job -> !oldJobsLinks.contains(job.getLink()))
                .collect(Collectors.toList());
    }

    private boolean containsExcludedTerm(String title) {
        String lowerTitle = title.toLowerCase();
        return excludedTitleTerms.stream()
                .anyMatch(lowerTitle::contains);
    }



    public boolean validateDescription(String description) {


        if (description == null || description.isEmpty()) {
            System.out.println("Job invalid: Description is null or empty");
            return false;
        }

        String descLower = description.toLowerCase();

        // Check if description contains at least one position
        boolean hasPosition = appConfig.getPositions().stream()
                .anyMatch(position -> descLower.contains(position.toLowerCase()));

        if (!hasPosition) {
            System.out.println("Job invalid: No matching position found. Required positions: " +
                    appConfig.getPositions());
            return false;
        }

        // Check if description contains at least one level
        boolean hasLevel = appConfig.getLevels().stream()
                .anyMatch(level -> descLower.contains(level.toLowerCase()));

        if (!hasLevel) {
            System.out.println("Job invalid: No matching level found. Required levels: " +
                    appConfig.getLevels());
            return false;
        }

        // Check if description contains any excluded terms
        List<String> foundExcludedTerms = appConfig.getExcludedPageTerms().stream()
                .filter(excludedTerm -> descLower.contains(excludedTerm.toLowerCase())).toList();

        if (!foundExcludedTerms.isEmpty()) {
            System.out.println("Job invalid: Contains excluded terms: " + foundExcludedTerms);
            return false;
        }

        System.out.println("Job valid: All criteria met");
        return true;
    }

    public boolean validateDescriptionNoLogs(String description) {
        if (description == null || description.isEmpty()) {
            return false;
        }

        String descLower = description.toLowerCase();

        // Must have at least one position AND one level
        // Must NOT have any excluded terms
        return appConfig.getPositions().stream()
                .anyMatch(position -> descLower.contains(position.toLowerCase()))
                && appConfig.getLevels().stream()
                .anyMatch(level -> descLower.contains(level.toLowerCase()))
                && appConfig.getExcludedPageTerms().stream()
                .noneMatch(excludedTerm -> descLower.contains(excludedTerm.toLowerCase()));
    }
}

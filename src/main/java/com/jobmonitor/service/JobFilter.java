package com.jobmonitor.service;

import com.jobmonitor.model.Job;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JobFilter {
    private final List<String> excludedTitleTerms;

    public JobFilter(List<String> excludedTitleTerms) {
        this.excludedTitleTerms = excludedTitleTerms.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
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
}

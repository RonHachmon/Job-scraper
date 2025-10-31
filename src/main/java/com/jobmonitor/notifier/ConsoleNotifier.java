package com.jobmonitor.notifier;

import com.jobmonitor.model.Job;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsoleNotifier implements Notifier {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SEPARATOR = "=".repeat(80);

    @Override
    public void notify(List<Job> jobs) {
        String message = formatMessage(jobs);
        System.out.println("\n" + SEPARATOR);
        System.out.println("NEW JOB POSTINGS - " + LocalDateTime.now().format(FORMATTER));
        System.out.println(SEPARATOR);
        System.out.println(message);
        System.out.println(SEPARATOR + "\n");
    }

    private String formatMessage(List<Job> jobs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(jobs.size()).append(" new job(s):\n\n");
        
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            sb.append(i + 1).append(". ").append(job.getTitle()).append("\n");
            sb.append(job.getLink()).append("\n\n");
        }
        
        return sb.toString();
    }
}

package com.jobmonitor.model;

public class Job {
    private final String link;
    private final String title;
    private final String snippet;

    public Job(String link, String title, String snippet) {
        this.link = link;
        this.title = title;
        this.snippet = snippet;
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public String toString() {
        return "Job{" +
                "link='" + link + '\'' +
                ", title='" + title + '\'' +
                ", snippet='" + snippet + '\'' +
                '}';
    }
}

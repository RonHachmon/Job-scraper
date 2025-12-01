package com.jobmonitor.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jobmonitor.config.AppConfig;
import com.jobmonitor.model.Job;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleSearchService implements JobsProvider {
    private static final String BASE_URL = "https://www.googleapis.com/customsearch/v1";
    private static final int MAX_PAGES = 10;
    
    private final AppConfig config;
    private final HttpClient httpClient;
    private final JobFilter jobFilter;

    public GoogleSearchService(AppConfig config, JobFilter jobFilter) {
        this.config = config;
        this.jobFilter = jobFilter;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public List<Job> fetchJobs() throws Exception {
        List<Job> allJobs = new ArrayList<>();
        
        for (int page = 1; page <= MAX_PAGES; page++) {
            String url = buildApiUrl(page);

            String response = executeRequest(url);
            
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            
            if (page == 1) {
                logSearchInfo(json);
            }
            
            List<Job> jobs = parseJobs(json);
            allJobs.addAll(jobs);
            
            if (!hasNextPage(json)) {
                break;
            }
        }
        allJobs = jobFilter.filterByTitle(allJobs);
        
        return allJobs;
    }

    private String buildApiUrl(int page) {
        String searchQuery = buildSearchQuery();

        List<String> excludedTerms = config.getExcludedPageTerms(); // List of strings to exclude

        // Transform ["3+", "3 years"] to ["- "3+"", "- "3 years""]
        List<String> negatedTerms = excludedTerms.stream()
                // 1. Prepend the minus sign (-)
                // 2. Wrap the term in quotes (") for robustness
                .map(term -> "-\"" + term + "\"")
                .collect(Collectors.toList());

        // C. Join everything with spaces
        String exclusionString = String.join(" ", negatedTerms);

        String full_query = searchQuery + " " + exclusionString;


        int startIndex = (page - 1) * 10 + 1;


        return BASE_URL + "?" +
                "q=" + encode(full_query) +
                //"&excludeTerms=" + encode(excludedTerms) +
                "&gl=" + config.getCountryCode() +
                "&cx=" + config.getSearchEngineId() +
                "&key=" + config.getApiKey() +
                "&start=" + startIndex +
                "&fields=" + encode("items(title,link,snippet),queries(nextPage,request(searchTerms,excludeTerms))");
    }

    private List<String> WrapQuote(List<String> strings) {
        if (strings == null) {
            return null;
        }

        return strings.stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.toList());
    }

    private String buildSearchQuery() {
        String positions = String.join(" OR ", WrapQuote(config.getPositions()));
        String levels = String.join(" OR ", WrapQuote(config.getLevels()));
        String locations = String.join(" OR ", WrapQuote(config.getLocations()));
        
        return String.format("(%s) (%s) (%s)", positions, levels, locations);
    }

    private String executeRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("API request failed with status: " + response.statusCode());
        }
        
        return response.body();
    }

    private List<Job> parseJobs(JsonObject json) {
        List<Job> jobs = new ArrayList<>();
        
        if (!json.has("items")) {
            return jobs;
        }
        
        JsonArray items = json.getAsJsonArray("items");
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            String link = item.get("link").getAsString();
            String title = item.get("title").getAsString();
            String snippet = item.get("snippet").getAsString();
            
            jobs.add(new Job(link, title, snippet));
        }
        
        return jobs;
    }

    private boolean hasNextPage(JsonObject json) {
        return json.has("queries") && 
               json.getAsJsonObject("queries").has("nextPage");
    }

    private void logSearchInfo(JsonObject json) {
        if (json.has("queries") && json.getAsJsonObject("queries").has("request")) {
            JsonObject request = json.getAsJsonObject("queries")
                    .getAsJsonArray("request")
                    .get(0)
                    .getAsJsonObject();
            
            System.out.println("Search terms: " + request.get("searchTerms").getAsString());
            if (request.has("excludeTerms")) {
                System.out.println("Excluded terms: " + request.get("excludeTerms").getAsString());
            }
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

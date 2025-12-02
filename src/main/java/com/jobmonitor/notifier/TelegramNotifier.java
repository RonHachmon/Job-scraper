package com.jobmonitor.notifier;

import com.jobmonitor.model.Job;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TelegramNotifier implements Notifier {
    private static final int CHUNK_SIZE = 10;
    private static final String TELEGRAM_API = "https://api.telegram.org/bot";
    
    private final String botToken;
    private final String chatId;
    private final HttpClient httpClient;

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void notify(List<Job> jobs) throws Exception {

        if (jobs.size() == 0) {
            sendMessage("No new jobs");
            return;
        }

        sendMessage("Found " + jobs.size() + " new jobs\n\n");

        int currentIndex = 1;
        for (int i = 0; i < jobs.size(); i += CHUNK_SIZE) {
            List<Job> chunk = jobs.subList(i, Math.min(i + CHUNK_SIZE, jobs.size()));
            String message = formatMessage(chunk, currentIndex);
            sendMessage(message);
            currentIndex += chunk.size();
        }

    }

    private String formatMessage(List<Job> jobs, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            sb.append(startIndex + i).append(". ").append(job.getTitle()).append("\n");
            sb.append(job.getLink()).append("\n\n");
        }
        
        return sb.toString();
    }

    private void sendMessage(String text) throws Exception {
        String url = TELEGRAM_API + botToken + "/sendMessage";
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String requestBody = "chat_id=" + chatId + "&text=" + encodedText;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

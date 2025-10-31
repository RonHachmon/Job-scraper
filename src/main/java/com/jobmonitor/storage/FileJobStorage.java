package com.jobmonitor.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class FileJobStorage implements JobStorage {
    private final String filePath;
    private final Gson gson;

    public FileJobStorage(String filePath) {
        this.filePath = filePath;
        this.gson = new Gson();
    }

    @Override
    public Set<String> getStoredJobLinks() {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            return new HashSet<>();
        }

        try {
            String content = Files.readString(path);
            Type setType = new TypeToken<Set<String>>(){}.getType();
            return gson.fromJson(content, setType);
        } catch (IOException e) {
            System.err.println("Error reading job links: " + e.getMessage());
            return new HashSet<>();
        }
    }

    @Override
    public void saveJobLinks(Set<String> links) {
        try {
            String json = gson.toJson(links);
            Files.writeString(Paths.get(filePath), json);
        } catch (IOException e) {
            System.err.println("Error saving job links: " + e.getMessage());
        }
    }
}

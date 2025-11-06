package com.jobmonitor.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileJobStorage implements JobStorage {
    private final String filePath;

    public FileJobStorage(String filePath) {
        this.filePath = filePath;

    }

    @Override
    public Set<String> getStoredJobLinks() {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new HashSet<>();
        }
        try {
            List<String> lines = Files.readAllLines(path);
            return new HashSet<>(lines);
        } catch (IOException e) {
            System.err.println("Error reading job links: " + e.getMessage());
            return new HashSet<>();
        }
    }

    @Override
    public void saveJobLinks(Set<String> links) {
        if (links.isEmpty()) {
            return;
        }

        Path path = Paths.get(filePath);

        try {
            Files.write(path, links, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error saving job links: " + e.getMessage());
        }
    }


}

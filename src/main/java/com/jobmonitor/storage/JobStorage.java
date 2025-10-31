package com.jobmonitor.storage;

import java.util.Set;

public interface JobStorage {
    Set<String> getStoredJobLinks();
    void saveJobLinks(Set<String> links);
}

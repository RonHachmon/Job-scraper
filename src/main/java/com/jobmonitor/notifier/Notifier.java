package com.jobmonitor.notifier;

import com.jobmonitor.model.Job;
import java.util.List;

public interface Notifier {
    void notify(List<Job> jobs) throws Exception;
}

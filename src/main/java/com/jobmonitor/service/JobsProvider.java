package com.jobmonitor.service;

import com.jobmonitor.model.Job;

import java.util.List;

public interface JobsProvider {

     List<Job> fetchJobs() throws Exception;
}

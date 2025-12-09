package com.jobmonitor.service.scrapers;

import com.jobmonitor.model.Job;
import com.jobmonitor.selenium.BrowserType;
import com.jobmonitor.selenium.WebDriverFactory;
import com.jobmonitor.service.JobFilter;
import com.jobmonitor.service.JobsProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RedHatScraper implements JobsProvider {

    private static final String CAREERS_URL =
            "https://redhat.wd5.myworkdayjobs.com/jobs/?a=084562884af243748dad7c84c304d89a";


    private static final String JOBS_POSTED_TODAY_XPATH =
            "//ul[@role=\"list\" and contains(@aria-label, 'Page')]//dd[contains(text(),'Today')]";

    private static final String JOB_LINK_XPATH_FROM_DATE = "../../../../..//h3//a";

    private static final String JOB_LINK_XPATH = "//h3//a";
    private static final String NEXT_PAGE_BUTTON_XPATH = "//button[contains(@aria-label, 'next')]";
    private static final String JOB_DESCRIPTION_XPATH = "//div[@data-automation-id='jobPostingDescription']//p";

    private static final int MAX_PER_PAGE = 20;

    private final JobFilter filter;
    private WebDriver driver;


    public RedHatScraper(JobFilter filter) {
        this.filter = filter;
    }

    @Override
    public List<Job> fetchJobs(){
        initializeWebDriver();

        try {
            return scrapeAndFilterJobs();
        } finally {
            cleanupWebDriver();
        }
    }

    private void initializeWebDriver() {
        this.driver = WebDriverFactory.createDriver(BrowserType.FIREFOX);

    }

    private void cleanupWebDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    private List<Job> scrapeAndFilterJobs() {
        navigateToCareersSite();
        List<Job> jobs = extractAllJobs();
        List<Job> jobs1 = filter.filterByTitle(jobs);

        return filterJobsByDescription(jobs);
    }

    private void navigateToCareersSite() {
        driver.get(CAREERS_URL);
    }

    private List<Job> extractAllJobs() {
        List<Job> jobs = new ArrayList<>();

        do {
            jobs.addAll(extractJobsFromCurrentPage());
        } while (navigateToNextPage());

        return jobs;
    }

    private List<Job> extractJobsFromCurrentPage() {
        List<Job> jobs = new ArrayList<>();
        List<WebElement> jobElements = findJobsPostedToday();

        for (WebElement element : jobElements) {
            Job job = extractJobFromElement(element);
            jobs.add(job);
        }

        return jobs;
    }

    private List<WebElement> findJobsPostedToday() {
        return driver.findElements(By.xpath(JOBS_POSTED_TODAY_XPATH));
    }

    private Job extractJobFromElement(WebElement element) {
        WebElement jobLink = element.findElement(By.xpath(JOB_LINK_XPATH_FROM_DATE));
        String title = jobLink.getText();
        String link = jobLink.getAttribute("href");
        return new Job(link, title, "");
    }

    private boolean navigateToNextPage() {

        int totalElementsInPage = driver.findElements(By.xpath(JOB_LINK_XPATH)).size();

        if(totalElementsInPage<MAX_PER_PAGE){
            return false;
        }

        List<WebElement> nextButtons = driver.findElements(By.xpath(NEXT_PAGE_BUTTON_XPATH));

        if (nextButtons.isEmpty()) {
            return false;
        }

        nextButtons.get(0).click();
        return true;
    }


    @SuppressWarnings("unused")
    private List<Job> filterJobsByDescription(List<Job> jobs) {
        List<Job> filteredJobs = new ArrayList<>();

        for (Job job : jobs) {
            if (jobMatchesDescriptionCriteria(job)) {
                filteredJobs.add(job);
            }
        }

        return filteredJobs;
    }

    private boolean jobMatchesDescriptionCriteria(Job job) {
        driver.get(job.getLink());
        String fullDescription = buildFullJobDescription(job);
        System.out.println(fullDescription);

        return filter.validateDescription(fullDescription);
    }

    private String buildFullJobDescription(Job job) {
        StringBuilder description = new StringBuilder();
        description.append(job.getTitle()).append("\n");

        List<WebElement> paragraphs = driver.findElements(By.xpath(JOB_DESCRIPTION_XPATH));
        for (WebElement paragraph : paragraphs) {
            description.append(paragraph.getText()).append("\n");
        }

        return description.toString();
    }
}
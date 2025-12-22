package com.jobmonitor.service.scrapers;

import com.jobmonitor.model.Job;
import com.jobmonitor.selenium.BrowserType;
import com.jobmonitor.selenium.WebDriverFactory;
import com.jobmonitor.service.JobFilter;
import com.jobmonitor.service.JobsProvider;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;


public class AppleScraper implements JobsProvider {

    private static final String APPLE_JOBS_URL = "https://jobs.apple.com/en-il/search?sort=newest&location=israel-ISR";

    // Possible teams
    //    Machine Learning and AI
    //    Hardware
    //    Software and Services
    //    Design
    //    Operations and Supply Chain
    //    Marketing
    //    Corporate Functions
    //    Apple Retail
    //    Sales and Business Development
    //    Support and Service
    //    Students
    private static final String TARGET_TEAM = "Software and Services";

    // Total pages to Scrape
    private static final int TOTAL_PAGES_TO_SCRAPE = 3;
    private static final int WAIT_TIMEOUT_SECONDS = 10;

    // XPath Selectors
    private static final String JOB_LIST_ITEMS = "//ul[@id='search-job-list']//li[@class='rc-accordion-item']";
    private static final String TEAM_NAME = ".//span[@class='team-name mt-0']";
    private static final String TITLE_LINK = ".//h3//a";
    private static final String NEXT_PAGE_BUTTON = "//button[@aria-label='Next Page']";

    private final JobFilter filter;
    private WebDriver driver;
    private WebDriverWait wait;

    public AppleScraper(JobFilter filter) {
        this.filter = filter;
    }

    @Override
    public List<Job> fetchJobs() throws Exception {
        try {
            initializeDriver();
            navigateToJobsPage();
            List<Job> jobs = scrapeMultiplePages();
            return filterJobs(jobs);
        } finally {
            closeDriver();
        }
    }

    private void initializeDriver() {
        driver = WebDriverFactory.createDriver(BrowserType.FIREFOX);
        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
    }

    private void navigateToJobsPage() {
        driver.get(APPLE_JOBS_URL);
    }

    private List<Job> scrapeMultiplePages() {
        List<Job> allJobs = new ArrayList<>();

        for (int pageNumber = 0; pageNumber < TOTAL_PAGES_TO_SCRAPE; pageNumber++) {
            List<WebElement> currentPageElements = findJobElements();
            List<Job> pageJobs = scrapeJobsFromElements(currentPageElements);
            allJobs.addAll(pageJobs);

            if (!navigateToNextPage(currentPageElements)) {
                break;
            }
        }

        return allJobs;
    }

    private List<Job> scrapeJobsFromElements(List<WebElement> jobElements) {
        List<Job> jobs = new ArrayList<>();

        for (WebElement jobElement : jobElements) {
            extractJobIfRelevant(jobElement).ifPresent(jobs::add);
        }

        return jobs;
    }

    private List<WebElement> findJobElements() {
        return driver.findElements(By.xpath(JOB_LIST_ITEMS));
    }

    private Optional<Job> extractJobIfRelevant(WebElement jobElement) {
        try {
            if (!isTargetTeam(jobElement)) {
                return Optional.empty();
            }

            return Optional.of(extractJobDetails(jobElement));
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    private boolean isTargetTeam(WebElement jobElement) {
        String teamName = jobElement.findElement(By.xpath(TEAM_NAME)).getText();
        return TARGET_TEAM.equals(teamName);
    }

    private Job extractJobDetails(WebElement jobElement) {
        WebElement titleElement = jobElement.findElement(By.xpath(TITLE_LINK));
        String url = titleElement.getAttribute("href");
        String title = titleElement.getText();

        return new Job(url, title, "Apple");
    }

    private boolean navigateToNextPage(List<WebElement> oldPageElements) {
        if (!clickNextPageButton()) {
            return false;
        }

        waitForPageToChange(oldPageElements);
        return true;
    }

    private boolean clickNextPageButton() {
        try {
            WebElement nextButton = driver.findElement(By.xpath(NEXT_PAGE_BUTTON));

            if (isNextPageDisabled(nextButton)) {
                return false;
            }

            nextButton.click();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean isNextPageDisabled(WebElement nextButton) {
        String ariaDisabled = nextButton.getAttribute("aria-disabled");
        return "true".equals(ariaDisabled);
    }

    private void waitForPageToChange(List<WebElement> oldPageElements) {
        if (!oldPageElements.isEmpty()) {
            wait.until(ExpectedConditions.stalenessOf(oldPageElements.get(0)));
        }
    }

    private List<Job> filterJobs(List<Job> jobs) {
        return filter.filterByTitle(jobs);
    }

    private void closeDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
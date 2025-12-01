package com.jobmonitor.service.scrapers;

import com.jobmonitor.model.Job;
import com.jobmonitor.selenium.BrowserType;
import com.jobmonitor.selenium.WebDriverFactory;
import com.jobmonitor.service.JobFilter;
import com.jobmonitor.service.JobsProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ImpervaScraper implements JobsProvider {

    private static final String CAREERS_URL = "https://careers.thalesgroup.com/global/en/search-results";
    private static final String ISRAEL = "Israel";

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final JobFilter filter;

    private int totalPages;

    public ImpervaScraper(JobFilter filter) {
        this.driver = WebDriverFactory.createDriver(BrowserType.FIREFOX, true);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.filter =filter;

    }

    @Override
    public List<Job> fetchJobs() {
        try {
            navigateToCareersSite();
            declineCookies();
            filterByCountry();
            countPages();
            List<Job> jobs = scrapeAllJobPages();

            return filteredJobs(jobs);
        } finally {
            driver.quit();
        }
    }

    private void countPages() {
        List<WebElement> paginationPages = driver.findElements(By.xpath("//li[@class='au-target']"));
        if(paginationPages.size()==0){
            this.totalPages = 1;
        }
        else {
            this.totalPages = 1 + paginationPages.size() ;
        }
    }

    private List<Job> filteredJobs(List<Job> jobs) {
        List<Job> jobs1 = filter.filterByTitle(jobs);

        List<Job> jobs2 = filterAllJobsByDescription(jobs1);

        printJobSummary(jobs2);
        return jobs2;
    }

    private void navigateToCareersSite() {
        driver.get(CAREERS_URL);
    }

    private void declineCookies() {
        try {
            WebElement declineButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[.//ppc-content[contains(text(),'Decline')]]")
                    )
            );
            declineButton.click();
        } catch (Exception e) {
            // No cookie banner present
        }
    }

    private void filterByCountry() {
        openCountryFilter();
        WebElement firstJobBeforeFilter = captureFirstJob();
        selectCountry();
        waitForFilterToApply(firstJobBeforeFilter);
    }

    private void openCountryFilter() {
        WebElement countryButton = driver.findElement(
                By.xpath("//button[contains(text(), 'Country')]")
        );
        countryButton.click();
    }

    private WebElement captureFirstJob() {
        return driver.findElement(By.xpath("//li[@class='jobs-list-item']"));
    }

    private void selectCountry() {
        String xpath = String.format(
                "//li[@data-ph-at-id = 'facet-results-item' and .//span[contains(text(), '%s')]]//label",
                ImpervaScraper.ISRAEL
        );
        WebElement countryOption = driver.findElement(By.xpath(xpath));
        countryOption.click();
    }

    private void waitForFilterToApply(WebElement oldElement) {
        wait.until(ExpectedConditions.stalenessOf(oldElement));
    }

    private List<Job> scrapeAllJobPages() {
        List<Job> jobs = new ArrayList<>();

        for(int i = 0; i<totalPages;++i){
            jobs.addAll(scrapeCurrentPage());

            if(i!= totalPages -1){
                goToNextPage();
            }

        }

        return jobs;
    }

    private boolean hasMorePages() {
        return !findJobListings().isEmpty();
    }

    private List<Job> scrapeCurrentPage() {
        List<Job> jobs = new ArrayList<>();
        List<WebElement> jobListings = findJobListings();

        for (WebElement listing : jobListings) {
            Job job = extractJobFromListing(listing);
            jobs.add(job);
        }

        return jobs;
    }

    private List<Job> filterAllJobsByDescription(List<Job> jobs){
        List<Job> filteredJob = new ArrayList<>();
        for(Job job: jobs){
            driver.get(job.getLink());

            List<WebElement> elements = driver.findElements(By.xpath("//div[@class='jd-info au-target']//p"));

            StringBuilder fullDescription = new StringBuilder();
            fullDescription.append(job.getTitle()).append("\n");
            for(WebElement paragraph : elements){
                fullDescription.append(paragraph.getText()).append("\n");
            }
            String description = fullDescription.toString();

            if(filter.validateDescription(description)){
                filteredJob.add(job);
                
            }
        }
        return filteredJob;

    }



    private List<WebElement> findJobListings() {
        return driver.findElements(By.xpath("//li[@class='jobs-list-item']"));
    }

    private Job extractJobFromListing(WebElement listing) {
        String title = extractTitle(listing);
        String description = extractDescription(listing);
        String url = extractUrl(listing);

        return new Job(url, title, description);
    }

    private String extractTitle(WebElement listing) {
        return listing.findElement(
                By.xpath(".//div[@class='job-title']//span")
        ).getText();
    }

    private String extractDescription(WebElement listing) {
        return listing.findElement(
                By.xpath(".//p[@class='au-target job-description show']")
        ).getText();
    }

    private String extractUrl(WebElement listing) {
        return listing.findElement(By.xpath(".//a")).getAttribute("href");
    }

    private void printJobSummary(List<Job> jobs) {

        for(Job job : jobs) {
            System.out.println("-".repeat(80));
            System.out.println(job.getTitle());
            System.out.println("-".repeat(80));
            System.out.println(job.getLink());
            System.out.println(job.getSnippet());
            System.out.println();
        }
    }


    private void goToNextPage() {
        WebElement firstJobOnCurrentPage = driver.findElement(
                By.xpath("//li[@class='jobs-list-item'][1]")
        );

        driver.findElement(By.xpath("//a[@class='next-btn au-target']")).click();

        wait.until(ExpectedConditions.stalenessOf(firstJobOnCurrentPage));
    }
}
package com.jobmonitor.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.time.Duration;

public class WebDriverFactory {

    private WebDriverFactory() {
        throw new UnsupportedOperationException("Utility class");
    }



    public static WebDriver createDriver(BrowserType browserType) {
        return createDriver(browserType, false);
    }

    public static WebDriver createDriver(BrowserType browserType, boolean headless) {
        WebDriver driver;

        switch (browserType) {
            case CHROME -> {
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                driver = new ChromeDriver(chromeOptions);
            }
            case FIREFOX -> {
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("--headless");
                    firefoxOptions.addArguments("--no-sandbox");
                    firefoxOptions.addArguments("--disable-dev-shm-usage");
                }
                driver = new FirefoxDriver(firefoxOptions);
            }
            case EDGE -> {
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless=new");
                }
                driver = new EdgeDriver(edgeOptions);
            }
            case SAFARI -> {
                // Safari doesn't support headless mode
                SafariOptions safariOptions = new SafariOptions();
                driver = new SafariDriver(safariOptions);
            }
            default -> throw new IllegalArgumentException("Unsupported browser type: " + browserType);
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

        return driver;
    }
}
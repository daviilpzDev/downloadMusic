package org.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.ChromeOptionsUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Hooks {
    public static WebDriver driver;
    public static WebDriverWait wait;
    public static String downloadFilepath;

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        downloadFilepath = System.getProperty("user.dir") + "/target/";
        driver = new ChromeDriver(ChromeOptionsUtil.getChromeOptions(downloadFilepath));
        wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

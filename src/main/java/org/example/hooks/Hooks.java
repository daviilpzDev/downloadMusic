package org.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.utils.ChromeOptionsUtil;
import org.example.utils.postactions.PostActions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class Hooks {
    public static WebDriver driver;
    public static final String downloadFilepath = System.getenv("DOWNLOAD_PATH") != null
            ? System.getenv("DOWNLOAD_PATH")
            : "/mnt/storage/media/music/";

    @Before
    public void setUp() {
        try {
            driver = new RemoteWebDriver(new URL(System.getenv("SELENIUM_URL")), ChromeOptionsUtil.getChromeOptions(downloadFilepath));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error en la URL de Selenium Grid", e);
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

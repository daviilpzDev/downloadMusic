package org.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.example.utils.ChromeOptionsUtil;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            takeScreenshot(scenario.getName());
        }

        if (driver != null) {
            driver.quit();
        }
    }

    // Método para tomar capturas de pantalla
    private void takeScreenshot(String screenshotName) {
        TakesScreenshot screenshotTaker = (TakesScreenshot) driver;
        File screenshot = screenshotTaker.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(screenshot, new File("./screenshots/" + screenshotName + ".png"));
        } catch (IOException e) {
            System.out.println("Error al guardar la captura de pantalla: " + e.getMessage());
        }
    }
}

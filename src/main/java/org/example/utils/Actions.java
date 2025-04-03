package org.example.utils;

import org.openqa.selenium.By;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.Map;

public class Actions {
    public static void clickAndSendText(WebDriverWait wait, By locator, String text) {
        clickElement(wait, locator);
        writeText(wait, locator, text);
    }

    public static void clickElement(WebDriverWait wait, By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    public static void writeText(WebDriverWait wait, By locator, String text) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).sendKeys(text);
    }

    public static List<String> getYmlFile(String fileName) {
        Yaml yaml = new Yaml();
        String filePath = System.getProperty("user.dir") + "/src/test/resources/data/" + fileName + ".yml";

        try {
            FileInputStream inputStream = new FileInputStream(filePath);

            if (inputStream == null) {
                throw new RuntimeException("File yml not found");
            }

            Map<String, Object> config = yaml.load(inputStream);
            List<String> ymlFile = (List<String>) config.get(fileName);

            return ymlFile;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void waitForPageLoad (WebDriverWait wait) {
        wait.until((driver) -> {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String readyState = js.executeScript("return document.readyState").toString();
            return readyState.equals("complete");
        });
    }
}

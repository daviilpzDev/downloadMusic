package org.example.utils;

import org.example.Methods;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import java.time.Duration;

import static org.example.hooks.Hooks.driver;

public class Constants {
    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(Methods.class);
    public static final WebDriverWait WAIT_SUPER_FAST = new WebDriverWait(driver, Duration.ofSeconds(3));
    public static final WebDriverWait WAIT_FAST = new WebDriverWait(driver, Duration.ofSeconds(5));
    public static final WebDriverWait WAIT_FASTER = new WebDriverWait(driver, Duration.ofSeconds(10));
    public static final WebDriverWait WAIT_MEDIUM = new WebDriverWait(driver, Duration.ofSeconds(15));
    public static final WebDriverWait WAIT_SLOW = new WebDriverWait(driver, Duration.ofSeconds(30));
    public static final String downloadFilepath = System.getProperty("user.dir") + "/target/";
}

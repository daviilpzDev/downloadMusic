package org.example;

import org.openqa.selenium.By;

public class Locators {
    public static final By SEARCH_INPUT = By.xpath("//input[@name='search_query']");
    public static final By FIRST_RESULT = By.xpath("(//a[@id='video-title'])[1]");
    public static final By ONLY_VIDEOS_BUTTON = By.xpath("//div[@id='chip-container']//*[@id='text' and contains(text(), 'Videos')]");
    public static final By FILTER_BUTTON = By.xpath("//div[@id='filter-button']");
    public static final By ONLY_VIDEOS_FILTER_BUTTON = By.xpath("//div[@id='label' and @title='Search Video']");
    public static final By POPUP_BUTTON_YT = By.xpath("//button[contains(@aria-label, 'Accept')]");
}

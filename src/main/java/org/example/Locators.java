package org.example;

import org.openqa.selenium.By;

public class Locators {
    public static final By SEARCH_INPUT = By.xpath("//input[@name='search_query']");
    public static final By FIRST_RESULT = By.xpath("(//a[@id='video-title'])[1]");
    public static final By EMAIL_INPUT = By.xpath("//input[@id='usernameInput']");
    public static final By PASSWORD_INPUT = By.xpath("//input[@id='passwordInput']");
    public static final By LOGIN_BUTTON = By.xpath("//input[@id='loginButton']");
    public static final By LAUNCH_BUTTON = By.xpath("//div[@class='serviceIcon hasTooltip']");
    public static final By INPUT_LINKS_BUTTON = By.xpath("//a[@class='gwt-Button' and contains(text(), 'links')]");
    public static final By INPUT_LINKS_FIELD = By.xpath("//textarea[@class='GHS0TFHC0']");
    public static final By CONTINUE_BUTTON = By.xpath("//button[@class='gwt-Button' and contains(text(), 'Continue')]");
    public static final By LIST_DOWNLOADS = By.xpath("//div[@class='GHS0TFHP2 listRow']");
    public static final By LIST_DOWNLOADS_SELECTED = By.xpath("//*[contains(@class, 'rowSelected')]");
    public static final By ACTION_BUTTON = By.xpath("//a[@title='Actions']");
    public static final By START_DOWNLOAD_BUTTON = By.xpath("//a[contains(@title, 'Start download')]");
//    public static final By PROGRESS_BAR = By.xpath("//p[contains(text(), '100%')]");


    public static final By POPUP_BUTTON = By.xpath("//a[contains(text(), 'thanks')]");
    public static final By POPUP_BUTTON_YT = By.xpath("//button[contains(@aria-label, 'Aceptar')]");
}

package org.example.utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Helper {

    public static boolean isElementVisible(WebDriverWait wait, By locator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String loadSMB(){
        Dotenv dotenv = Dotenv.load();
        String smbUser = dotenv.get("SMB_USER");
        String smbPassword = dotenv.get("SMB_PASSWORD");
        String smbHost = dotenv.get("SMB_HOST");
        String smbPath = dotenv.get("SMB_PATH");

        String smbUrlBase = "smb://" + smbUser + ":" + smbPassword + "@" + smbHost;
        return smbUrlBase + smbPath + "/";
    }
}

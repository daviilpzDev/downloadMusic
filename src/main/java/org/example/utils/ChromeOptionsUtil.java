package org.example.utils;

import org.openqa.selenium.chrome.ChromeOptions;
import java.util.HashMap;
import java.util.Map;

public class ChromeOptionsUtil {
    public static ChromeOptions getChromeOptions(String downloadFilepath) {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", downloadFilepath);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--no-sandbox");
        options.addArguments("--maximized");
//        options.addArguments("--headless");
        options.addArguments("--incognito");
        options.addArguments("--mute-audio");
        return options;
    }
}

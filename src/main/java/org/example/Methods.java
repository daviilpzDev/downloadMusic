package org.example;

import org.example.utils.Actions;
import org.example.utils.Constants;
import org.example.utils.Globals;
import org.example.utils.Helper;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Methods {

    private static final Logger logger = Constants.logger;

    public static void searchSongAndGetTheUrl(WebDriver driver) {
        List<String> songs = Actions.getYmlFile("songs");

        List<String> urls = Globals.list;
        driver.get("https://www.youtube.com/");

        Actions.clickElement(Constants.WAIT_SLOW, Locators.POPUP_BUTTON_YT);

        for (String song : songs) {
            driver.get("https://www.youtube.com/");
            try {
                WebElement searchBox = Constants.WAIT_SLOW.until(ExpectedConditions.elementToBeClickable(Locators.SEARCH_INPUT));
                searchBox.sendKeys(song + Keys.ENTER);

                if (Helper.isElementVisible(Constants.WAIT_SUPER_FAST, Locators.ONLY_VIDEOS_BUTTON)) {
                    Actions.clickElement(Constants.WAIT_FAST, Locators.ONLY_VIDEOS_BUTTON);
                } else {
                    Actions.clickElement(Constants.WAIT_SUPER_FAST, Locators.FILTER_BUTTON);
                    Actions.clickElement(Constants.WAIT_FAST, Locators.ONLY_VIDEOS_FILTER_BUTTON);
                }

                Constants.WAIT_SLOW.until(ExpectedConditions.visibilityOfElementLocated(Locators.FIRST_RESULT));
                String videoUrl = driver.findElement(Locators.FIRST_RESULT).getDomAttribute("href");

                urls.add(videoUrl);
            } catch (Exception e) {
                logger.error("Song searching error: " + song + ": " + e.getMessage());
            }
        }
        driver.quit();
    }

    public static void downloadSongAction() {
        List<String> songs = Globals.list;
        for (String song : songs) {
            try {
                String[] command = new String[]{
                        "yt-dlp", "-x", "--audio-format", "mp3", "-o", Constants.downloadFilepath + "%(title)s.%(ext)s" , "https://www.youtube.com"+ song
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line);
                }

                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = errorReader.readLine()) != null) {
                    logger.error(line);
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    logger.info("¡Descarga completada exitosamente!");
                } else {
                    logger.error("Hubo un error en la descarga.");
                }

            } catch (Exception e) {
                logger.error("Song downloading error: " + getSong(song) + ": " + e.getMessage());
            }
        }
    }

    private static String getSong(String url) {
        List<String> songs = Actions.getYmlFile("songs");
        List<String> urls = Globals.list;

        if (songs.size() != urls.size()) {
            return "Las listas de canciones y URLs tienen tamaños diferentes.";
        }

        Map<String, String> map = IntStream.range(0, songs.size())
                .boxed()
                .collect(Collectors.toMap(urls::get, songs::get));

        return map.getOrDefault(url, "Canción no encontrada");
    }

}

package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.logging.Log;
import org.example.utils.Actions;
import org.example.utils.Globals;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.example.hooks.Hooks.driver;

public class Methods {

    public static void searchSongAndGetTheUrl(WebDriverWait wait) {
        List<String> songs = Actions.getYmlFile("songs");

        List<String> urls = Globals.list;
        driver.get("https://www.youtube.com/");

        Actions.clickElement(wait, Locators.POPUP_BUTTON_YT);

        for (String song : songs) {
            driver.get("https://www.youtube.com/");
            try {
                WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(Locators.SEARCH_INPUT));

                searchBox.sendKeys(song + Keys.ENTER);

                WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(Locators.FIRST_RESULT));
                firstResult.click();

                wait.until(ExpectedConditions.urlContains("youtube.com/watch"));

                String videoUrl = driver.getCurrentUrl();

                urls.add(videoUrl);
            } catch (Exception e) {
                System.err.println("Song searching error: " + song + ": " + e.getMessage());
            }
        }
    }

    public static void jdownloaderLogin(WebDriverWait wait) {
        List<String> text = Actions.getYmlFile("loginJd");

        Actions.clickAndSendText(wait, Locators.EMAIL_INPUT, text.get(0));
        Actions.clickAndSendText(wait, Locators.PASSWORD_INPUT, text.get(1));
        Actions.clickElement(wait, Locators.LOGIN_BUTTON);
        Actions.clickElement(wait, Locators.POPUP_BUTTON);
        Actions.clickElement(wait, Locators.LAUNCH_BUTTON);
    }

    public static void downloadSongAction(WebDriverWait wait) {
        List<String> songs = Globals.list;

        for (String song : songs) {
            Actions.clickElement(wait, Locators.INPUT_LINKS_BUTTON);
            Actions.clickAndSendText(wait, Locators.INPUT_LINKS_FIELD, song);
            Actions.clickElement(wait, Locators.CONTINUE_BUTTON);
            wait.until(ExpectedConditions.urlContains("links"));

            List<WebElement> downloadFilepath = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(Locators.LIST_DOWNLOADS));

            for (WebElement element : downloadFilepath) {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'output')]")));
                wait.until(ExpectedConditions.visibilityOfAllElements(element));
                wait.until(ExpectedConditions.elementToBeClickable(element)).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(Locators.LIST_DOWNLOADS_SELECTED));
                Actions.clickElement(wait, Locators.ACTION_BUTTON);
                Actions.clickElement(wait, Locators.START_DOWNLOAD_BUTTON);
                wait.until(ExpectedConditions.urlContains("downloads"));
            }
        }

    }

    public static void deleteNotNeededFiles() {
        try {
            Dotenv dotenv = Dotenv.load();
            String smbUser = dotenv.get("SMB_USER");
            String smbPassword = dotenv.get("SMB_PASSWORD");
            String smbHost = dotenv.get("SMB_HOST");
            String smbPath = dotenv.get("SMB_PATH");

            String smbUrl = "smb://" + smbUser + ":" + smbPassword + "@" + smbHost + smbPath + "/";

            SmbFile smbFolder = new SmbFile(smbUrl);

            if (!smbFolder.exists() || !smbFolder.isDirectory()) {
                System.out.println("La carpeta de SMB no existe o no es un directorio.");
                return;
            }

            SmbFile[] files = smbFolder.listFiles();

            if (files == null || files.length == 0) {
                System.out.println("No se encontraron archivos en la carpeta SMB.");
                return;
            }

            String[] allowedExtensions = {".mp3", ".opus", ".aac", ".m4a"};

            for (SmbFile file : files) {
                if (!file.isFile()) continue;

                String fileName = file.getName().toLowerCase();

                boolean isAllowed = false;
                for (String ext : allowedExtensions) {
                    if (fileName.endsWith(ext)) {
                        isAllowed = true;
                        break;
                    }
                }

                if (!isAllowed) {
                    System.out.println("Eliminando archivo no permitido: " + fileName);
                    file.delete();
                }
            }

            System.out.println("Proceso de limpieza de SMB completado.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

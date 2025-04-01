package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class Methods {
    public static void clickAndSend(WebDriverWait wait, By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
        element.sendKeys(text);
    }

    public static void clickElement(WebDriverWait wait, By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
    }

    public static void moveFileToSMB(final String downloadFilepath) {
        try {
            Dotenv dotenv = Dotenv.load();
            String smbUser = dotenv.get("SMB_USER");
            String smbPassword = dotenv.get("SMB_PASSWORD");
            String smbHost = dotenv.get("SMB_HOST");
            String smbPath = dotenv.get("SMB_PATH");

            File folder = new File(downloadFilepath);
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".mp3"));

            if (files == null || files.length == 0) {
                System.out.println("No se encontraron archivos MP3.");
                return;
            }

            File localFile = files[0];
            String smbUrl = "smb://" + smbUser + ":" + smbPassword + "@" + smbHost + smbPath + "/" + localFile.getName();

            SmbFile smbFile = new SmbFile(smbUrl);
            try (FileInputStream fis = new FileInputStream(localFile);
                 OutputStream os = new SmbFileOutputStream(smbFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) > 0) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("Archivo movido a SMB: " + smbUrl);
            localFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

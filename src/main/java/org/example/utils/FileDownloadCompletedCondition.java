package org.example.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class FileDownloadCompletedCondition implements ExpectedCondition<Boolean> {
    private final String downloadPath;
    private long lastSize = -1;

    public FileDownloadCompletedCondition(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    @Override
    public Boolean apply(WebDriver driver) {
        File folder = new File(downloadPath);
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        boolean hasPartial = Arrays.stream(files)
                .anyMatch(file -> file.getName().endsWith(".crdownload"));
        if (hasPartial) {
            return false;
        }
        File latestFile = Arrays.stream(files)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
        if (latestFile != null) {
            long currentSize = latestFile.length();
            if (currentSize > 0 && currentSize == lastSize) {
                return true;
            }
            lastSize = currentSize;
        }
        return false;
    }

}

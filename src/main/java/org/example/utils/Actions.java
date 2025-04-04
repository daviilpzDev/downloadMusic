package org.example.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.Map;

public class Actions {

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

}

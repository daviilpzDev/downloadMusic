package org.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {
    public static final String downloadFilepath = System.getenv("DOWNLOAD_PATH") != null
            ? System.getenv("DOWNLOAD_PATH")
            : "/mnt/storage/media/music/";

    @Before
    public void setUp() {

    }

    @After
    public void tearDown(Scenario scenario) {

    }

}

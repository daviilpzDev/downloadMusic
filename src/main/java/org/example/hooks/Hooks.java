package org.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.util.Optional;

public class Hooks {
    public static final String downloadFilepath = Optional.ofNullable(System.getenv("DOWNLOAD_PATH"))
            .map(path -> path.endsWith("/") ? path : path + "/")
            .orElse(System.getProperty("user.dir") + "/target/");

    @Before
    public void setUp() {

    }

    @After
    public void tearDown(Scenario scenario) {

    }

}

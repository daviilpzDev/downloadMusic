package org.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.example.utils.Constants;

public class Hooks {
    public static final String downloadFilepath = Constants.DOWNLOAD_PATH;

    @Before
    public void setUp() {
        // Configuración inicial si es necesaria
    }

    @After
    public void tearDown(Scenario scenario) {
        // Limpieza después de cada escenario si es necesaria
    }
}

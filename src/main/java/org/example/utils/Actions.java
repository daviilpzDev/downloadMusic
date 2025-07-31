package org.example.utils;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Actions {
    private static final Logger logger = Constants.logger;

    public static List<String> getYmlFile(String fileName) {
        Yaml yaml = new Yaml();
        String filePath = Constants.YML_BASE_PATH + "/" + fileName + Constants.YML_EXTENSION;

        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            Map<String, Object> config = yaml.load(inputStream);
            
            if (config == null) {
                throw new RuntimeException("El archivo YAML está vacío o mal formateado: " + filePath);
            }

            @SuppressWarnings("unchecked")
            List<String> ymlFile = (List<String>) config.get(fileName);
            
            if (ymlFile == null) {
                throw new RuntimeException("No se encontró la clave '" + fileName + "' en el archivo: " + filePath);
            }

            logger.info("Cargadas {} canciones desde {}", ymlFile.size(), filePath);
            return ymlFile;

        } catch (FileNotFoundException e) {
            String errorMsg = "Archivo YAML no encontrado: " + filePath;
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Error al leer el archivo YAML: " + filePath;
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (ClassCastException e) {
            String errorMsg = "Formato incorrecto en el archivo YAML. Se esperaba una lista de strings para la clave '" + fileName + "'";
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
}

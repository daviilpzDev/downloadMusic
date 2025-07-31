package org.example.utils;

import org.example.Methods;
import org.slf4j.Logger;

import java.util.Optional;

public class Constants {
    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(Methods.class);
    
    // Configuración de rutas
    public static final String DOWNLOAD_PATH = Optional.ofNullable(System.getenv("DOWNLOAD_PATH"))
            .map(path -> path.endsWith("/") ? path : path + "/")
            .orElse(System.getProperty("user.dir") + "/target/");
    
    public static final String YML_BASE_PATH = System.getenv().getOrDefault("YML_PATH", 
            System.getProperty("user.dir") + "/src/test/resources/data");
    
    // Configuración de yt-dlp
    public static final String YT_DLP_COMMAND = "yt-dlp";
    public static final String AUDIO_FORMAT = "mp3";
    public static final String OUTPUT_FORMAT = "%(title)s.%(ext)s";
    
    // Configuración de archivos
    public static final String YML_EXTENSION = ".yml";
    public static final String MP3_EXTENSION = ".mp3";
    
    // Mensajes de log
    public static final String LOG_SONG_FOUND = "Found URL for song: {} - {}";
    public static final String LOG_NO_URL_FOUND = "No URL found for song: {}";
    public static final String LOG_SEARCH_ERROR = "Song searching error: {}: {}";
    public static final String LOG_DOWNLOAD_ERROR = "Song downloading error: {}: {}";
    public static final String LOG_DOWNLOAD_SUCCESS = "¡Descarga completada exitosamente!";
    public static final String LOG_FILE_RENAMED = "Archivo renombrado a: {}";
    public static final String LOG_RENAME_FAILED = "No se pudo renombrar el archivo: {}";
    public static final String LOG_FILE_NOT_DETECTED = "No se detectó el archivo descargado para: {}";
    public static final String LOG_DIFFERENT_SIZES = "Las listas de canciones y URLs tienen tamaños diferentes.";
}

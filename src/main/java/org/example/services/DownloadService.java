package org.example.services;

import org.example.utils.Constants;
import org.example.utils.Globals;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class DownloadService {
    private static final Logger logger = Constants.logger;
    private Set<String> downloadedFiles = new HashSet<>();

    public void searchSongsAndGetUrls(List<String> songs) {
        if (songs == null || songs.isEmpty()) {
            logger.warn("Lista de canciones vacía o nula");
            return;
        }

        List<String> urls = Globals.list;
        Set<String> uniqueUrls = new HashSet<>();
        int duplicatesDetected = 0;
        
        logger.info("=== INICIANDO BÚSQUEDA DE URLs ===");
        logger.info("Total de canciones a procesar: {}", songs.size());

        for (int i = 0; i < songs.size(); i++) {
            String song = songs.get(i);
            if (song == null || song.trim().isEmpty()) {
                logger.warn("Canción nula o vacía, saltando...");
                continue;
            }

            logger.info("🔄 Procesando canción {}/{}: '{}'", i + 1, songs.size(), song);

            try {
                String[] command = new String[]{
                        Constants.YT_DLP_COMMAND, "ytsearch:" + song.trim(), "--get-id"
                };

                logger.debug("Comando generado: {}", String.join(" ", command));

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String videoId = reader.readLine();

                if (videoId != null && !videoId.isEmpty()) {
                    String fullUrl = "https://www.youtube.com/watch?v=" + videoId;
                    
                    // Verificar si la URL ya existe
                    if (uniqueUrls.contains(fullUrl)) {
                        duplicatesDetected++;
                        logger.warn(Constants.LOG_DUPLICATE_URL_DURING_SEARCH, fullUrl, song);
                        logger.info("🔄 Saltando URL duplicada para evitar conflictos");
                    } else {
                        uniqueUrls.add(fullUrl);
                        urls.add(fullUrl);
                        logger.info("✅ URL encontrada para '{}': {}", song, fullUrl);
                        System.out.println("✅ Found URL for song: " + song + " - " + fullUrl);
                    }
                } else {
                    logger.warn("❌ No se encontró URL para: {}", song);
                }

                process.waitFor();
            } catch (Exception e) {
                logger.error("❌ Error buscando canción '{}': {}", song, e.getMessage());
            }
        }

        if (duplicatesDetected > 0) {
            logger.info(Constants.LOG_SEARCH_DUPLICATES_SUMMARY, duplicatesDetected);
        }

        logger.info("=== BÚSQUEDA COMPLETADA ===");
        logger.info("URLs únicas encontradas: {}/{}", urls.size(), songs.size());
    }

    public void downloadSongs(List<String> urls, List<String> songs) {
        if (urls == null || songs == null) {
            logger.error("Las listas de URLs o canciones son nulas");
            return;
        }

        if (urls.isEmpty() || songs.isEmpty()) {
            logger.error("Las listas de URLs o canciones están vacías");
            return;
        }

        logger.info("=== INICIANDO DESCARGA DE CANCIONES ===");
        logger.info("Total de canciones originales: {}", songs.size());
        logger.info("Total de URLs originales: {}", urls.size());

        Map<String, String> urlToSongMap = buildUrlToSongMap(urls, songs);
        
        // Usar solo las URLs únicas del mapa
        List<String> uniqueUrls = new ArrayList<>(urlToSongMap.keySet());
        logger.info("Total de URLs únicas a descargar: {}", uniqueUrls.size());

        for (int i = 0; i < uniqueUrls.size(); i++) {
            String url = uniqueUrls.get(i);
            String songNameRaw = urlToSongMap.get(url);
            
            logger.info("🎵 Descargando canción {}/{}: '{}'", i + 1, uniqueUrls.size(), songNameRaw);

            try {
                downloadSingleSong(url, songNameRaw);
                logger.info("✅ Descarga completada: '{}'", songNameRaw);
            } catch (Exception e) {
                logger.error("❌ Error descargando '{}': {}", songNameRaw, e.getMessage());
            }
        }

        logger.info("=== DESCARGA COMPLETADA ===");
        logger.info("Proceso finalizado. Revisa la carpeta de descargas.");
    }

    private void downloadSingleSong(String url, String songNameRaw) throws Exception {
        logger.info("📥 Iniciando descarga de: '{}'", songNameRaw);
        
        String[] command = new String[]{
                Constants.YT_DLP_COMMAND, "-x", "--audio-format", Constants.AUDIO_FORMAT,
                "-o", Constants.DOWNLOAD_PATH + Constants.OUTPUT_FORMAT,
                url
        };

        logger.debug("Comando de descarga: {}", String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        String downloadedFilename = processDownloadOutput(process);
        processErrorStream(process);

        int exitCode = process.waitFor();
        if (exitCode == 0 && downloadedFilename != null) {
            logger.info("🎵 ¡Descarga completada exitosamente!");
            handleDuplicateFile(downloadedFilename, songNameRaw);
        } else {
            logger.error("❌ No se detectó el archivo descargado para: {}", songNameRaw);
        }
    }

    private void handleDuplicateFile(String downloadedFilename, String songNameRaw) {
        File downloadedFile = new File(downloadedFilename);
        String fileName = downloadedFile.getName();
        
        // Verificar si el archivo ya existe en el sistema
        File existingFile = new File(Constants.DOWNLOAD_PATH + fileName);
        
        if (existingFile.exists() && !downloadedFiles.contains(fileName)) {
            logger.warn(Constants.LOG_DUPLICATE_FILE_DETECTED, fileName);
            logger.info(Constants.LOG_DUPLICATE_FILE_KEPT, fileName);
            logger.info(Constants.LOG_DUPLICATE_FILE_OVERWRITTEN);
        }
        
        // Agregar a la lista de archivos descargados en esta sesión
        downloadedFiles.add(fileName);
    }

    private String processDownloadOutput(Process process) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String downloadedFilename = null;

        while ((line = reader.readLine()) != null) {
            logger.info(line);

            // Detectar archivo descargado desde la línea de destino
            if (line.toLowerCase().contains("[extractaudio] destination:")) {
                int index = line.toLowerCase().indexOf("destination:") + "destination:".length();
                downloadedFilename = line.substring(index).trim();
            }
            // Detectar cuando el archivo ya existe y no se convierte
            else if (line.toLowerCase().contains("not converting audio") && line.toLowerCase().contains(".mp3")) {
                int startIndex = line.indexOf("/");
                int endIndex = line.indexOf(".mp3") + 4;
                if (startIndex != -1 && endIndex != -1) {
                    downloadedFilename = line.substring(startIndex, endIndex);
                }
            }
            // Detectar archivo que ya ha sido descargado
            else if (line.toLowerCase().contains("has already been downloaded") && line.toLowerCase().contains(".mp3")) {
                int startIndex = line.indexOf("/");
                int endIndex = line.indexOf(".mp3") + 4;
                if (startIndex != -1 && endIndex != -1) {
                    downloadedFilename = line.substring(startIndex, endIndex);
                }
            }
        }
        return downloadedFilename;
    }

    private void processErrorStream(Process process) throws Exception {
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = errorReader.readLine()) != null) {
            logger.error(line);
        }
    }

    private Map<String, String> buildUrlToSongMap(List<String> urls, List<String> songs) {
        Map<String, String> urlToSongMap = new HashMap<>();
        int duplicatesDetected = 0;
        
        for (int i = 0; i < urls.size() && i < songs.size(); i++) {
            String url = urls.get(i);
            String song = songs.get(i);
            
            if (url != null && !url.trim().isEmpty()) {
                if (urlToSongMap.containsKey(url)) {
                    duplicatesDetected++;
                    logger.warn(Constants.LOG_DUPLICATE_URL_DETECTED, url, urlToSongMap.get(url), song);
                    logger.info("🔄 Manteniendo la primera canción y rechazando la duplicada");
                    // Mantener la primera canción asociada a esta URL
                    continue;
                }
                urlToSongMap.put(url, song);
                logger.debug("🔗 Mapeado URL: {} → '{}'", url, song);
            }
        }
        
        if (duplicatesDetected > 0) {
            logger.info(Constants.LOG_DUPLICATES_SUMMARY, duplicatesDetected);
        }
        
        logger.info("📊 Mapa URL→Canción creado con {} entradas únicas", urlToSongMap.size());
        return urlToSongMap;
    }
} 
package org.example.services;

import org.example.utils.Constants;
import org.example.utils.Globals;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DownloadService {
    private static final Logger logger = Constants.logger;

    public void searchSongsAndGetUrls(List<String> songs) {
        if (songs == null || songs.isEmpty()) {
            logger.warn("Lista de canciones vacía o nula");
            return;
        }

        List<String> urls = Globals.list;
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
                    urls.add(fullUrl);
                    logger.info("✅ URL encontrada para '{}': {}", song, fullUrl);
                    System.out.println("✅ Found URL for song: " + song + " - " + fullUrl);
                } else {
                    logger.warn("❌ No se encontró URL para: {}", song);
                }

                process.waitFor();
            } catch (Exception e) {
                logger.error("❌ Error buscando canción '{}': {}", song, e.getMessage());
            }
        }

        logger.info("=== BÚSQUEDA COMPLETADA ===");
        logger.info("URLs encontradas: {}/{}", urls.size(), songs.size());
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

        if (urls.size() != songs.size()) {
            logger.error(Constants.LOG_DIFFERENT_SIZES);
            return;
        }

        logger.info("=== INICIANDO DESCARGA DE CANCIONES ===");
        logger.info("Total de canciones a descargar: {}", songs.size());

        Map<String, String> urlToSongMap = buildUrlToSongMap(urls, songs);

        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            if (url == null || url.trim().isEmpty()) {
                logger.warn("URL nula o vacía, saltando...");
                continue;
            }

            String songNameRaw = urlToSongMap.getOrDefault(url, "Unknown");
            logger.info("🎵 Descargando canción {}/{}: '{}'", i + 1, urls.size(), songNameRaw);

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
            renameDownloadedFile(downloadedFilename, songNameRaw);
        } else {
            logger.error("❌ No se detectó el archivo descargado para: {}", songNameRaw);
        }
    }

    private String processDownloadOutput(Process process) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String downloadedFilename = null;

        while ((line = reader.readLine()) != null) {
            logger.info(line);

            if (line.toLowerCase().contains("[extractaudio] destination:")) {
                int index = line.toLowerCase().indexOf("destination:") + "destination:".length();
                downloadedFilename = line.substring(index).trim();
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

    private void renameDownloadedFile(String downloadedFilename, String songNameRaw) {
        File originalFile = new File(downloadedFilename);
        String sanitizedTargetName = sanitizeFilename(songNameRaw) + Constants.MP3_EXTENSION;
        File renamedFile = new File(Constants.DOWNLOAD_PATH + sanitizedTargetName);

        logger.info("🔄 Renombrando archivo: '{}' → '{}'", originalFile.getName(), renamedFile.getName());

        if (originalFile.renameTo(renamedFile)) {
            logger.info("✅ Archivo renombrado exitosamente: {}", renamedFile.getName());
        } else {
            logger.warn("⚠️ No se pudo renombrar el archivo: {}", originalFile.getName());
        }
    }

    private Map<String, String> buildUrlToSongMap(List<String> urls, List<String> songs) {
        return IntStream.range(0, songs.size())
                .boxed()
                .collect(Collectors.toMap(urls::get, songs::get));
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "").trim();
    }
} 
package org.example.services;

import lombok.extern.slf4j.Slf4j;
import org.example.utils.Actions;
import org.example.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.IntStream;

/**
 * Servicio de descarga real que funciona con las canciones del YAML
 */
@Slf4j
public class RealDownloadService {

    private final String outputDirectory;

    public RealDownloadService(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        // Crear directorio si no existe
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Método principal que carga canciones desde YAML y las descarga
     */
    public List<String> downloadSongsFromYaml() {
        log.info("🎵 === DESCARGA REAL DESDE SONGS.YML ===");
        
        // Cargar canciones desde YAML
        List<String> songs = Actions.getYmlFile("songs");
        
        if (songs == null || songs.isEmpty()) {
            log.error("❌ No se pudieron cargar canciones desde songs.yml");
            return new ArrayList<>();
        }
        
        log.info("📋 Canciones cargadas desde songs.yml:");
        for (int i = 0; i < songs.size(); i++) {
            log.info("   {}. {}", i + 1, songs.get(i));
        }
        
        return downloadSongs(songs);
    }

    /**
     * Descarga una lista de canciones
     */
    public List<String> downloadSongs(List<String> songs) {
        List<String> downloadedFiles = new ArrayList<>();
        
        if (songs == null || songs.isEmpty()) {
            log.warn("Lista de canciones vacía o nula");
            return downloadedFiles;
        }

        log.info("🔄 === INICIANDO DESCARGA ===");
        log.info("Total de canciones: {}", songs.size());
        log.info("Directorio de salida: {}", outputDirectory);

        for (int i = 0; i < songs.size(); i++) {
            String song = songs.get(i);
            if (song == null || song.trim().isEmpty()) {
                log.warn("Canción vacía, saltando...");
                continue;
            }

            log.info("🎵 Procesando {}/{}: '{}'", i + 1, songs.size(), song);

            try {
                String downloadedFile = downloadSingleSong(song.trim());
                if (downloadedFile != null) {
                    downloadedFiles.add(downloadedFile);
                    log.info("✅ Descarga exitosa: {}", new File(downloadedFile).getName());
                } else {
                    log.warn("❌ No se pudo descargar: '{}'", song);
                }
            } catch (Exception e) {
                log.error("❌ Error descargando '{}': {}", song, e.getMessage());
            }
        }

        log.info("📊 === RESUMEN FINAL ===");
        log.info("Procesadas: {}", songs.size());
        log.info("Exitosas: {}", downloadedFiles.size());
        log.info("Fallidas: {}", songs.size() - downloadedFiles.size());

        return downloadedFiles;
    }

    /**
     * Descarga una sola canción
     */
    private String downloadSingleSong(String song) throws Exception {
        log.info("🔍 Buscando: '{}'", song);

        // Primero intentar buscar la URL
        String videoId = searchForVideoId(song);
        if (videoId == null) {
            log.warn("⚠️ No se encontró video para: '{}'", song);
            return null;
        }

        String url = "https://www.youtube.com/watch?v=" + videoId;
        log.info("🎯 Video encontrado: {}", url);

        // Luego descargar
        return downloadFromUrl(url, song);
    }

    /**
     * Busca el ID del video en YouTube
     */
    private String searchForVideoId(String song) throws Exception {
        String[] command = new String[]{
            Constants.YT_DLP_COMMAND, 
            "ytsearch:" + song.trim(), 
            "--get-id",
            "--default-search", "auto"
        };

        log.debug("Comando búsqueda: {}", String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String videoId = reader.readLine();

        // Leer stderr para ver errores
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String errorLine;
        while ((errorLine = errorReader.readLine()) != null) {
            log.debug("yt-dlp error: {}", errorLine);
        }

        int exitCode = process.waitFor();
        
        if (exitCode == 0 && videoId != null && !videoId.trim().isEmpty()) {
            log.debug("✓ Video ID encontrado: {}", videoId);
            return videoId.trim();
        }

        return null;
    }

    /**
     * Descarga desde una URL específica
     */
    private String downloadFromUrl(String url, String originalSong) throws Exception {
        log.info("📥 Descargando desde: {}", url);

        String sanitizedName = sanitizeFilename(originalSong);
        String outputTemplate = outputDirectory + "/" + sanitizedName + ".%(ext)s";

        String[] command = new String[]{
            Constants.YT_DLP_COMMAND,
            url,
            "--extract-audio",
            "--audio-format", "mp3",
            "--audio-quality", "192K",
            "--output", outputTemplate,
            "--no-playlist"
        };

        log.debug("Comando descarga: {}", String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String downloadedFile = null;

        while ((line = reader.readLine()) != null) {
            log.debug("yt-dlp: {}", line);
            
            if (line.contains("[ExtractAudio] Destination:")) {
                downloadedFile = extractDestinationPath(line);
                log.debug("Archivo destino: {}", downloadedFile);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            // Si no capturamos el nombre del archivo, buscar en el directorio
            if (downloadedFile == null) {
                downloadedFile = findRecentDownload(sanitizedName);
            }

            if (downloadedFile != null && new File(downloadedFile).exists()) {
                log.info("💾 Archivo guardado: {}", downloadedFile);
                return downloadedFile;
            }
        }

        log.warn("⚠️ Descarga falló con código: {}", exitCode);
        return null;
    }

    /**
     * Extrae la ruta del archivo de la línea de destino
     */
    private String extractDestinationPath(String line) {
        String marker = "[ExtractAudio] Destination: ";
        int index = line.indexOf(marker);
        if (index >= 0) {
            return line.substring(index + marker.length()).trim();
        }
        return null;
    }

    /**
     * Busca el archivo más reciente en el directorio
     */
    private String findRecentDownload(String expectedName) {
        File dir = new File(outputDirectory);
        File[] files = dir.listFiles((file, name) -> 
            name.endsWith(".mp3") && name.toLowerCase().contains(expectedName.toLowerCase().substring(0, Math.min(expectedName.length(), 5)))
        );

        if (files != null && files.length > 0) {
            // Retornar el más reciente
            File newest = files[0];
            for (File file : files) {
                if (file.lastModified() > newest.lastModified()) {
                    newest = file;
                }
            }
            return newest.getAbsolutePath();
        }

        return null;
    }

    /**
     * Sanitiza el nombre del archivo
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9\\s\\-_]", "")
                      .replaceAll("\\s+", "_")
                      .trim();
    }
}
package org.example.services;

import lombok.extern.slf4j.Slf4j;
import org.example.utils.Constants;
import org.example.utils.Globals;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de descarga simple que funciona con yt-dlp
 */
@Slf4j
public class SimpleDownloadService {

    public List<String> searchAndDownloadSongs(List<String> songs, String outputDir) {
        List<String> downloadedFiles = new ArrayList<>();
        
        if (songs == null || songs.isEmpty()) {
            log.warn("Lista de canciones vacía o nula");
            return downloadedFiles;
        }

        // Crear directorio de salida si no existe
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        log.info("🎵 === INICIANDO DESCARGA DIRECTA ===");
        log.info("Total de canciones a procesar: {}", songs.size());
        log.info("Directorio de salida: {}", outputDir);

        for (int i = 0; i < songs.size(); i++) {
            String song = songs.get(i);
            if (song == null || song.trim().isEmpty()) {
                log.warn("Canción nula o vacía, saltando...");
                continue;
            }

            log.info("🔄 Procesando canción {}/{}: '{}'", i + 1, songs.size(), song);

            try {
                String downloadedFile = searchAndDownloadSingleSong(song.trim(), outputDir);
                if (downloadedFile != null) {
                    downloadedFiles.add(downloadedFile);
                    log.info("✅ Descarga exitosa: {}", downloadedFile);
                } else {
                    log.warn("❌ No se pudo descargar: '{}'", song);
                }
            } catch (Exception e) {
                log.error("❌ Error descargando '{}': {}", song, e.getMessage());
            }
        }

        log.info("📊 === RESUMEN DE DESCARGA ===");
        log.info("Total procesadas: {}", songs.size());
        log.info("Exitosas: {}", downloadedFiles.size());
        log.info("Fallidas: {}", songs.size() - downloadedFiles.size());

        return downloadedFiles;
    }

    private String searchAndDownloadSingleSong(String song, String outputDir) throws Exception {
        log.info("🔍 Buscando y descargando: '{}'", song);

        // Sanitizar nombre para el archivo
        String sanitizedName = sanitizeFilename(song);
        String outputTemplate = outputDir + "/" + sanitizedName + ".%(ext)s";

        String[] command = new String[]{
                Constants.YT_DLP_COMMAND,
                "ytsearch:" + song,
                "--extract-audio",
                "--audio-format", "mp3", 
                "--audio-quality", "192K",
                "--output", outputTemplate,
                "--no-playlist",
                "--max-downloads", "1"
        };

        log.debug("Comando generado: {}", String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Combinar stdout y stderr
        Process process = processBuilder.start();

        // Leer la salida del proceso
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String downloadedFile = null;
        
        while ((line = reader.readLine()) != null) {
            log.debug("yt-dlp: {}", line);
            
            // Buscar líneas que indiquen el archivo descargado
            if (line.contains("[ExtractAudio] Destination:")) {
                downloadedFile = extractFilename(line);
            } else if (line.contains("100%") && line.contains(".mp3")) {
                // Extraer nombre del archivo de la línea de progreso
                String filename = extractFilenameFromProgress(line);
                if (filename != null) {
                    downloadedFile = outputDir + "/" + filename;
                }
            }
        }

        int exitCode = process.waitFor();
        
        if (exitCode == 0) {
            // Si no se pudo extraer el nombre del log, buscar archivos mp3 recientes
            if (downloadedFile == null) {
                downloadedFile = findRecentMp3File(outputDir, sanitizedName);
            }
            
            if (downloadedFile != null && new File(downloadedFile).exists()) {
                log.info("📁 Archivo descargado: {}", downloadedFile);
                return downloadedFile;
            }
        }

        log.warn("⚠️ No se pudo completar la descarga de: '{}'", song);
        return null;
    }

    private String extractFilename(String line) {
        // Extraer el nombre del archivo de líneas como "[ExtractAudio] Destination: /path/file.mp3"
        int index = line.indexOf("Destination:");
        if (index >= 0) {
            return line.substring(index + "Destination:".length()).trim();
        }
        return null;
    }

    private String extractFilenameFromProgress(String line) {
        // Extraer nombre de archivo de líneas de progreso como "100% of 3.45MiB in 00:02 filename.mp3"
        if (line.contains(".mp3")) {
            String[] parts = line.split("\\s+");
            for (String part : parts) {
                if (part.endsWith(".mp3")) {
                    return part;
                }
            }
        }
        return null;
    }

    private String findRecentMp3File(String outputDir, String expectedName) {
        File dir = new File(outputDir);
        File[] files = dir.listFiles((file, name) -> 
            name.endsWith(".mp3") && name.toLowerCase().contains(expectedName.toLowerCase().substring(0, Math.min(expectedName.length(), 10)))
        );
        
        if (files != null && files.length > 0) {
            // Retornar el archivo más reciente
            File newestFile = files[0];
            for (File file : files) {
                if (file.lastModified() > newestFile.lastModified()) {
                    newestFile = file;
                }
            }
            return newestFile.getAbsolutePath();
        }
        
        return null;
    }

    private String sanitizeFilename(String filename) {
        // Remover caracteres problemáticos para nombres de archivo
        return filename.replaceAll("[^a-zA-Z0-9\\s\\-_]", "")
                      .replaceAll("\\s+", "_")
                      .trim();
    }
}
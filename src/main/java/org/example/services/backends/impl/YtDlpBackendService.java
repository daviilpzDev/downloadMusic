package org.example.services.backends.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.models.DownloadRequest;
import org.example.models.DownloadResult;
import org.example.models.VideoInfo;
import org.example.services.backends.BackendMetrics;
import org.example.services.backends.DownloadBackendService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementación del backend yt-dlp - Herramienta externa funcional
 */
@Slf4j
public class YtDlpBackendService implements DownloadBackendService {
    
    private final AtomicLong downloadCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    public YtDlpBackendService() {
        log.info("🚀 yt-dlp Backend Service iniciado");
    }
    
    @Override
    public Optional<VideoInfo> searchVideo(String searchTerm) {
        log.info("🔍 Buscando video con yt-dlp: '{}'", searchTerm);
        
        try {
            // Comando mejorado para buscar sin problemas de cookies
            String[] searchCommand = {
                "yt-dlp",
                "--no-warnings",
                "--quiet",
                "--print", "%(id)s|%(title)s|%(uploader)s|%(duration)s|%(webpage_url)s",
                "--default-search", "ytsearch1:",
                searchTerm
            };
            
            ProcessBuilder processBuilder = new ProcessBuilder(searchCommand);
            Process process = processBuilder.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && line != null && !line.trim().isEmpty()) {
                String[] parts = line.split("\\|", 5);
                if (parts.length >= 5) {
                    String videoId = parts[0];
                    String title = parts[1];
                    String uploader = parts[2];
                    String durationStr = parts[3];
                    String url = parts[4];
                    
                    Duration duration = parseDuration(durationStr);
                    
                    VideoInfo videoInfo = VideoInfo.builder()
                        .id(videoId)
                        .title(title)
                        .artist(uploader)
                        .url(url)
                        .duration(duration)
                        .quality(VideoInfo.VideoQuality.HIGH)
                        .uploadDate(LocalDateTime.now())
                        .build();
                    
                    log.info("✅ Video encontrado: '{}' por '{}' ({})", title, uploader, url);
                    return Optional.of(videoInfo);
                }
            }
            
            log.warn("⚠️ No se encontró video para: '{}'", searchTerm);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("❌ Error buscando video: {}", e.getMessage());
            errorCount.incrementAndGet();
            return Optional.empty();
        }
    }
    
    @Override
    public DownloadResult downloadAudio(DownloadRequest request) {
        downloadCount.incrementAndGet();
        long startTime = System.currentTimeMillis();
        
        VideoInfo videoInfo = request.getVideoInfo();
        String outputPath = request.getOutputPath();
        
        log.info("📥 Iniciando descarga con yt-dlp: '{}'", videoInfo.getTitle());
        
        try {
            // Crear directorio si no existe
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Sanitizar nombre de archivo
            String sanitizedTitle = sanitizeFilename(videoInfo.getSanitizedTitle());
            String outputTemplate = outputPath + "/" + sanitizedTitle + ".%(ext)s";
            
            String[] downloadCommand = {
                "yt-dlp",
                "--extract-audio",
                "--audio-format", "mp3",
                "--audio-quality", "192K",
                "--no-warnings",
                "--no-playlist",
                "--output", outputTemplate,
                videoInfo.getUrl()
            };
            
            log.debug("Ejecutando comando: {}", String.join(" ", downloadCommand));
            
            ProcessBuilder processBuilder = new ProcessBuilder(downloadCommand);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Leer output para logging
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("100%") || line.contains("download")) {
                    log.debug("yt-dlp: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            long downloadTime = System.currentTimeMillis() - startTime;
            
            if (exitCode == 0) {
                // Buscar archivo descargado
                File[] files = outputDir.listFiles((dir, name) -> 
                    name.startsWith(sanitizedTitle) && name.endsWith(".mp3"));
                
                if (files != null && files.length > 0) {
                    File downloadedFile = files[0];
                    successCount.incrementAndGet();
                    
                    DownloadResult result = DownloadResult.builder()
                        .success(true)
                        .filePath(downloadedFile.getAbsolutePath())
                        .fileName(downloadedFile.getName())
                        .fileSize(downloadedFile.length())
                        .backendUsed("yt-dlp (External Tool)")
                        .downloadTimeMs(downloadTime)
                        .downloadedAt(LocalDateTime.now())
                        .metadata(DownloadResult.DownloadMetadata.builder()
                            .originalUrl(videoInfo.getUrl())
                            .videoTitle(videoInfo.getTitle())
                            .videoId(videoInfo.getId())
                            .build())
                        .build();
                    
                    log.info("✅ Descarga exitosa: {} ({})", downloadedFile.getName(), 
                           formatFileSize(downloadedFile.length()));
                    return result;
                }
            }
            
            errorCount.incrementAndGet();
            return DownloadResult.builder()
                .success(false)
                .backendUsed("yt-dlp (External Tool)")
                .downloadTimeMs(downloadTime)
                .errorMessage("Fallo en descarga, código de salida: " + exitCode)
                .downloadedAt(LocalDateTime.now())
                .build();
            
        } catch (Exception e) {
            long downloadTime = System.currentTimeMillis() - startTime;
            errorCount.incrementAndGet();
            
            log.error("❌ Error en descarga: {}", e.getMessage());
            return DownloadResult.builder()
                .success(false)
                .backendUsed("yt-dlp (External Tool)")
                .downloadTimeMs(downloadTime)
                .errorMessage("Error: " + e.getMessage())
                .downloadedAt(LocalDateTime.now())
                .build();
        }
    }
    
    @Override
    public List<VideoInfo> searchVideos(List<String> searchTerms) {
        List<VideoInfo> results = new ArrayList<>();
        for (String term : searchTerms) {
            searchVideo(term).ifPresent(results::add);
        }
        return results;
    }
    
    @Override
    public Optional<VideoInfo> getVideoInfo(String url) {
        // Implementación básica
        return Optional.empty();
    }
    
    @Override
    public BackendMetrics getMetrics() {
        long total = downloadCount.get();
        long success = successCount.get();
        double successRate = total > 0 ? (success * 100.0 / total) : 0.0;
        
        return BackendMetrics.builder()
            .backendName("yt-dlp (External Tool)")
            .totalDownloads(total)
            .successfulDownloads(success)
            .failedDownloads(errorCount.get())
            .successRate(successRate)
            .averageDownloadTimeMs(0L) // Se podría calcular
            .isAvailable(isAvailable())
            .lastUsed(LocalDateTime.now())
            .version("2025.03.27")
            .build();
    }
    
    @Override
    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("yt-dlp", "--version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getBackendName() {
        return "yt-dlp (External Tool)";
    }
    
    @Override
    public boolean supportsUrl(String url) {
        return url.contains("youtube.com") || url.contains("youtu.be") || url.contains("ytsearch:");
    }
    
    private Duration parseDuration(String durationStr) {
        try {
            if (durationStr == null || durationStr.equals("NA")) {
                return Duration.ofMinutes(3); // Duración por defecto
            }
            long seconds = Long.parseLong(durationStr);
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            return Duration.ofMinutes(3);
        }
    }
    
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9\\-_\\s]", "").trim();
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
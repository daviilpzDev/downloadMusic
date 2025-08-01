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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementación del backend yt-dlp - Herramienta externa tradicional
 */
@Slf4j
public class YtDlpBackendService implements DownloadBackendService {
    
    private static final String YT_DLP_COMMAND = "yt-dlp";
    private final AtomicLong downloadCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong totalDownloadTimeMs = new AtomicLong(0);
    
    public YtDlpBackendService() {
        log.info("🚀 yt-dlp Backend Service iniciado");
    }
    
    @Override
    public Optional<VideoInfo> searchVideo(String searchTerm) {
        log.info("🔍 Buscando video con yt-dlp: '{}'", searchTerm);
        
        try {
            String searchQuery = searchTerm.contains("youtube.com") || searchTerm.contains("youtu.be") 
                ? searchTerm 
                : "ytsearch1:" + searchTerm;
            
            String[] command = {YT_DLP_COMMAND, searchQuery, "--get-id"};
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String videoId = reader.readLine();
            
            if (videoId != null && !videoId.isEmpty()) {
                String fullUrl = "https://www.youtube.com/watch?v=" + videoId;
                return getVideoInfo(fullUrl);
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("❌ Error buscando video con yt-dlp: {}", e.getMessage());
            errorCount.incrementAndGet();
            return Optional.empty();
        }
    }
    
    @Override
    public List<VideoInfo> searchVideos(List<String> searchTerms) {
        log.info("🔍 Buscando {} videos con yt-dlp", searchTerms.size());
        
        return searchTerms.parallelStream()
                .map(this::searchVideo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
    
    @Override
    public DownloadResult downloadAudio(DownloadRequest request) {
        log.info("📥 Descargando audio con yt-dlp: '{}'", request.getVideoInfo().getTitle());
        downloadCount.incrementAndGet();
        
        long startTime = System.currentTimeMillis();
        
        try {
            File outputDir = new File(request.getOutputPath());
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            String outputTemplate = outputDir.getAbsolutePath() + File.separator + "%(title)s.%(ext)s";
            
            String[] command = {
                YT_DLP_COMMAND,
                "-x",
                "--audio-format", "mp3",
                "--audio-quality", "192K",
                "-o", outputTemplate,
                request.getVideoInfo().getUrl()
            };
            
            log.debug("🛠️ Comando yt-dlp: {}", String.join(" ", command));
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            
            // Procesar output del proceso
            String downloadedFilename = processDownloadOutput(process);
            processErrorStream(process);
            
            int exitCode = process.waitFor();
            long downloadTime = System.currentTimeMillis() - startTime;
            totalDownloadTimeMs.addAndGet(downloadTime);
            
            if (exitCode == 0 && downloadedFilename != null) {
                // Renombrar archivo si es necesario
                File downloadedFile = new File(downloadedFilename);
                String targetFilename = request.getTargetFilename();
                File targetFile = new File(outputDir, targetFilename);
                
                if (!downloadedFile.getName().equals(targetFilename)) {
                    if (downloadedFile.renameTo(targetFile)) {
                        downloadedFile = targetFile;
                    }
                }
                
                successCount.incrementAndGet();
                log.info("✅ Descarga yt-dlp completada en {}ms: {}", downloadTime, downloadedFile.getName());
                
                return DownloadResult.builder()
                        .success(true)
                        .filePath(downloadedFile.getAbsolutePath())
                        .fileName(downloadedFile.getName())
                        .fileSize(downloadedFile.length())
                        .backendUsed("yt-dlp")
                        .downloadTimeMs(downloadTime)
                        .downloadedAt(LocalDateTime.now())
                        .build();
            } else {
                throw new RuntimeException("yt-dlp falló con código de salida: " + exitCode);
            }
            
        } catch (Exception e) {
            long downloadTime = System.currentTimeMillis() - startTime;
            log.error("❌ Error descargando con yt-dlp en {}ms: {}", downloadTime, e.getMessage());
            errorCount.incrementAndGet();
            
            return DownloadResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .backendUsed("yt-dlp")
                    .downloadTimeMs(downloadTime)
                    .build();
        }
    }
    
    @Override
    public Optional<VideoInfo> getVideoInfo(String url) {
        log.info("📋 Obteniendo info de video con yt-dlp: {}", url);
        
        try {
            String[] command = {YT_DLP_COMMAND, url, "--dump-json", "--no-download"};
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder jsonOutput = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && jsonOutput.length() > 0) {
                // Parse manual del JSON (sin usar Jackson por simplicidad aquí)
                VideoInfo videoInfo = parseVideoInfoFromJson(jsonOutput.toString(), url);
                log.info("✅ Info obtenida con yt-dlp: '{}'", videoInfo.getTitle());
                return Optional.of(videoInfo);
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo info con yt-dlp: {}", e.getMessage());
            errorCount.incrementAndGet();
            return Optional.empty();
        }
    }
    
    private VideoInfo parseVideoInfoFromJson(String json, String url) {
        // Parse básico sin Jackson - en producción usaríamos Jackson
        String title = extractJsonField(json, "title");
        String id = extractJsonField(json, "id");
        String uploader = extractJsonField(json, "uploader");
        String durationStr = extractJsonField(json, "duration");
        
        Duration duration = Duration.ZERO;
        if (durationStr != null && !durationStr.equals("null")) {
            try {
                duration = Duration.ofSeconds(Long.parseLong(durationStr));
            } catch (NumberFormatException e) {
                log.warn("No se pudo parsear duración: {}", durationStr);
            }
        }
        
        return VideoInfo.builder()
                .id(id)
                .title(title != null ? title : "Unknown")
                .url(url)
                .duration(duration)
                .uploader(uploader)
                .uploadDate(LocalDateTime.now())
                .quality(VideoInfo.VideoQuality.HIGH)
                .build();
    }
    
    private String extractJsonField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Para campos numéricos sin comillas
        pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*([^,}\\]]+)");
        matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
    
    private String processDownloadOutput(Process process) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String downloadedFilename = null;
        
        while ((line = reader.readLine()) != null) {
            log.debug("yt-dlp output: {}", line);
            
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
            log.debug("yt-dlp error: {}", line);
        }
    }
    
    @Override
    public boolean supportsUrl(String url) {
        // yt-dlp soporta muchos sitios, pero principalmente verificamos YouTube
        return url.contains("youtube.com") || url.contains("youtu.be") || url.contains("ytsearch:");
    }
    
    @Override
    public String getBackendName() {
        return "yt-dlp (External Tool)";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(YT_DLP_COMMAND, "--version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            log.error("❌ yt-dlp no está disponible: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public BackendMetrics getMetrics() {
        long avgTime = downloadCount.get() > 0 ? totalDownloadTimeMs.get() / downloadCount.get() : 0;
        
        return BackendMetrics.builder()
                .backendName("yt-dlp")
                .totalDownloads(downloadCount.get())
                .successfulDownloads(successCount.get())
                .failedDownloads(errorCount.get())
                .successRate(calculateSuccessRate())
                .averageDownloadTimeMs(avgTime)
                .isAvailable(isAvailable())
                .lastUsed(LocalDateTime.now())
                .build();
    }
    
    private double calculateSuccessRate() {
        long total = downloadCount.get();
        if (total == 0) return 0.0;
        return (double) successCount.get() / total * 100.0;
    }
}
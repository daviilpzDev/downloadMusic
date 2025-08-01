package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.DownloadBackend;
import org.example.models.DownloadRequest;
import org.example.models.DownloadResult;
import org.example.models.VideoInfo;
import org.example.services.backends.BackendFactory;
import org.example.services.backends.DownloadBackendService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Demostración de rendimiento comparativo entre JTube y yt-dlp
 */
@Slf4j
public class PerformanceDemo {
    
    public static void main(String[] args) {
        log.info("🚀 === DEMOSTRACIÓN DE RENDIMIENTO: JTUBE vs YT-DLP ===");
        
        PerformanceDemo demo = new PerformanceDemo();
        demo.runComparison();
    }
    
    public void runComparison() {
        List<String> testUrls = List.of(
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ", // Rick Astley - Never Gonna Give You Up
                "https://www.youtube.com/watch?v=kJQP7kiw5Fk", // Luis Fonsi - Despacito  
                "https://www.youtube.com/watch?v=JGwWNGJdvx8"  // Ed Sheeran - Shape of You
        );
        
        log.info("📋 URLs de prueba: {}", testUrls.size());
        
        // Probar JTube (simulado)
        BackendResult jtubeResult = testBackend(DownloadBackend.JTUBE, testUrls);
        
        // Probar yt-dlp
        BackendResult ytdlpResult = testBackend(DownloadBackend.YT_DLP, testUrls);
        
        // Mostrar comparación
        displayComparison(jtubeResult, ytdlpResult);
    }
    
    private BackendResult testBackend(DownloadBackend backendType, List<String> testUrls) {
        log.info("🔄 Probando backend: {}", backendType.getDescription());
        
        DownloadBackendService backend = BackendFactory.createBackend(backendType);
        
        long totalTime = 0;
        int successCount = 0;
        int failCount = 0;
        List<String> downloadedFiles = new ArrayList<>();
        
        for (int i = 0; i < testUrls.size(); i++) {
            String url = testUrls.get(i);
            log.info("🎵 Procesando {}/{}: {}", i + 1, testUrls.size(), url);
            
            long startTime = System.currentTimeMillis();
            
            try {
                // 1. Obtener información del video
                var videoInfoOpt = backend.getVideoInfo(url);
                if (videoInfoOpt.isEmpty()) {
                    log.warn("⚠️ No se pudo obtener información del video");
                    failCount++;
                    continue;
                }
                
                VideoInfo videoInfo = videoInfoOpt.get();
                log.info("📋 Video: '{}'", videoInfo.getTitle());
                
                // 2. Crear request de descarga
                DownloadRequest request = DownloadRequest.builder()
                        .videoInfo(videoInfo)
                        .outputPath("./target/demo-" + backendType.getName() + "/")
                        .audioFormat(DownloadRequest.AudioFormat.MP3)
                        .audioQuality(DownloadRequest.AudioQuality.MEDIUM)
                        .overwriteExisting(true)
                        .build();
                
                // 3. Descargar
                DownloadResult result = backend.downloadAudio(request);
                
                long endTime = System.currentTimeMillis();
                long downloadTime = endTime - startTime;
                totalTime += downloadTime;
                
                if (result.isSuccess()) {
                    successCount++;
                    downloadedFiles.add(result.getFileName());
                    log.info("✅ Descarga exitosa en {}ms: {}", downloadTime, result.getFileName());
                } else {
                    failCount++;
                    log.error("❌ Descarga fallida: {}", result.getErrorMessage());
                }
                
            } catch (Exception e) {
                failCount++;
                log.error("❌ Error: {}", e.getMessage());
            }
            
            // Pausa entre descargas
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return new BackendResult(
                backendType.getDescription(),
                testUrls.size(),
                successCount,
                failCount,
                totalTime,
                downloadedFiles
        );
    }
    
    private void displayComparison(BackendResult jtube, BackendResult ytdlp) {
        log.info("");
        log.info("🏆 === COMPARACIÓN FINAL DE RENDIMIENTO ===");
        log.info("");
        
        displayBackendResult("JTube (Java Nativo)", jtube);
        log.info("");
        displayBackendResult("yt-dlp (Herramienta Externa)", ytdlp);
        
        log.info("");
        log.info("📊 === ANÁLISIS COMPARATIVO ===");
        
        if (jtube.successCount > 0 && ytdlp.successCount > 0) {
            double jtubeAvg = (double) jtube.totalTimeMs / jtube.successCount;
            double ytdlpAvg = (double) ytdlp.totalTimeMs / ytdlp.successCount;
            
            if (jtubeAvg < ytdlpAvg) {
                double speedup = ytdlpAvg / jtubeAvg;
                log.info("🥇 JTube es {} veces más rápido que yt-dlp", String.format("%.1f", speedup));
            } else {
                double speedup = jtubeAvg / ytdlpAvg;
                log.info("🥇 yt-dlp es {} veces más rápido que JTube", String.format("%.1f", speedup));
            }
        } else if (jtube.successCount > 0) {
            log.info("🥇 Solo JTube completó descargas exitosas");
        } else if (ytdlp.successCount > 0) {
            log.info("🥇 Solo yt-dlp completó descargas exitosas");
        } else {
            log.info("❌ Ningún backend completó descargas exitosas");
        }
        
        log.info("");
        log.info("💡 === CONCLUSIONES ===");
        log.info("📌 JTube (simulado): Biblioteca Java nativa, sin dependencias externas");
        log.info("📌 yt-dlp: Herramienta externa potente, soporta muchos sitios");
        log.info("📌 En producción real, los tiempos pueden variar según la red y el contenido");
        log.info("=====================================");
    }
    
    private void displayBackendResult(String name, BackendResult result) {
        log.info("🔧 Backend: {}", name);
        log.info("📊 Resultados:");
        log.info("   • Total procesadas: {}", result.totalProcessed);
        log.info("   • Exitosas: {}", result.successCount);
        log.info("   • Fallidas: {}", result.failCount);
        log.info("   • Tasa de éxito: {}%", String.format("%.1f", result.getSuccessRate()));
        log.info("   • Tiempo total: {}ms ({}s)", result.totalTimeMs, String.format("%.1f", result.totalTimeMs / 1000.0));
        
        if (result.successCount > 0) {
            long avgTime = result.totalTimeMs / result.successCount;
            log.info("   • Tiempo promedio: {}ms", avgTime);
        }
        
        if (!result.downloadedFiles.isEmpty()) {
            log.info("   • Archivos descargados: {}", result.downloadedFiles);
        }
    }
    
    // Clase interna para almacenar resultados
    private static class BackendResult {
        final String backendName;
        final int totalProcessed;
        final int successCount;
        final int failCount;
        final long totalTimeMs;
        final List<String> downloadedFiles;
        
        BackendResult(String backendName, int totalProcessed, int successCount, 
                     int failCount, long totalTimeMs, List<String> downloadedFiles) {
            this.backendName = backendName;
            this.totalProcessed = totalProcessed;
            this.successCount = successCount;
            this.failCount = failCount;
            this.totalTimeMs = totalTimeMs;
            this.downloadedFiles = new ArrayList<>(downloadedFiles);
        }
        
        double getSuccessRate() {
            return totalProcessed > 0 ? (double) successCount / totalProcessed * 100.0 : 0.0;
        }
    }
}
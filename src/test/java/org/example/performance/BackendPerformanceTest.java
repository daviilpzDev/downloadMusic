package org.example.performance;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.DownloadBackend;
import org.example.models.DownloadRequest;
import org.example.models.DownloadResult;
import org.example.models.VideoInfo;
import org.example.services.backends.BackendFactory;
import org.example.services.backends.DownloadBackendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Prueba de rendimiento para comparar diferentes backends de descarga
 */
@Slf4j
public class BackendPerformanceTest {
    
    private static final String TEST_OUTPUT_DIR = "./target/performance-test/";
    private static final List<String> TEST_SONGS = List.of(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ", // Rick Astley - Never Gonna Give You Up
            "https://www.youtube.com/watch?v=kJQP7kiw5Fk", // Luis Fonsi - Despacito  
            "https://www.youtube.com/watch?v=JGwWNGJdvx8"  // Ed Sheeran - Shape of You
    );
    
    private List<PerformanceResult> results;
    
    @BeforeEach
    void setUp() {
        // Crear directorio de pruebas
        File testDir = new File(TEST_OUTPUT_DIR);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        
        results = new ArrayList<>();
        log.info("🚀 Iniciando pruebas de rendimiento de backends");
    }
    
    @Test
    @EnabledIf("isJTubeAvailable")
    void testJTubePerformance() {
        log.info("🧪 Probando rendimiento de JTube...");
        
        DownloadBackendService jtubeBackend = BackendFactory.createBackend(DownloadBackend.JTUBE);
        PerformanceResult result = measureBackendPerformance(jtubeBackend, "JTube");
        
        results.add(result);
        logPerformanceResult(result);
    }
    
    @Test
    @EnabledIf("isYtDlpAvailable")
    void testYtDlpPerformance() {
        log.info("🧪 Probando rendimiento de yt-dlp...");
        
        DownloadBackendService ytDlpBackend = BackendFactory.createBackend(DownloadBackend.YT_DLP);
        PerformanceResult result = measureBackendPerformance(ytDlpBackend, "yt-dlp");
        
        results.add(result);
        logPerformanceResult(result);
    }
    
    @Test
    void compareAllAvailableBackends() {
        log.info("🏁 Comparando todos los backends disponibles...");
        
        List<PerformanceResult> allResults = new ArrayList<>();
        
        // Probar cada backend disponible
        for (DownloadBackend backendType : DownloadBackend.values()) {
            if (backendType.isAvailable()) {
                try {
                    log.info("🔄 Probando backend: {}", backendType.getDescription());
                    
                    DownloadBackendService backend = BackendFactory.createBackend(backendType);
                    PerformanceResult result = measureBackendPerformance(backend, backendType.getDescription());
                    
                    allResults.add(result);
                    logPerformanceResult(result);
                    
                } catch (Exception e) {
                    log.error("❌ Error probando backend {}: {}", backendType.getDescription(), e.getMessage());
                }
            } else {
                log.warn("⚠️ Backend no disponible: {}", backendType.getDescription());
            }
        }
        
        // Mostrar comparación final
        displayFinalComparison(allResults);
    }
    
    private PerformanceResult measureBackendPerformance(DownloadBackendService backend, String backendName) {
        log.info("📊 Midiendo rendimiento de: {}", backendName);
        
        long totalTime = 0;
        int successfulDownloads = 0;
        int failedDownloads = 0;
        long totalFileSize = 0;
        
        for (int i = 0; i < TEST_SONGS.size(); i++) {
            String testUrl = TEST_SONGS.get(i);
            log.info("🎵 Descargando canción {}/{} con {}: {}", i + 1, TEST_SONGS.size(), backendName, testUrl);
            
            try {
                long startTime = System.currentTimeMillis();
                
                // 1. Obtener información del video
                Optional<VideoInfo> videoInfoOpt = backend.getVideoInfo(testUrl);
                if (videoInfoOpt.isEmpty()) {
                    log.warn("⚠️ No se pudo obtener info del video: {}", testUrl);
                    failedDownloads++;
                    continue;
                }
                
                VideoInfo videoInfo = videoInfoOpt.get();
                log.info("📋 Video encontrado: '{}'", videoInfo.getTitle());
                
                // 2. Crear request de descarga
                DownloadRequest request = DownloadRequest.builder()
                        .videoInfo(videoInfo)
                        .outputPath(TEST_OUTPUT_DIR + backendName.toLowerCase().replace(" ", "-") + "/")
                        .audioFormat(DownloadRequest.AudioFormat.MP3)
                        .audioQuality(DownloadRequest.AudioQuality.MEDIUM)
                        .overwriteExisting(true)
                        .build();
                
                // 3. Descargar
                DownloadResult downloadResult = backend.downloadAudio(request);
                
                long endTime = System.currentTimeMillis();
                long downloadTime = endTime - startTime;
                totalTime += downloadTime;
                
                if (downloadResult.isSuccess()) {
                    successfulDownloads++;
                    totalFileSize += downloadResult.getFileSize();
                    log.info("✅ Descarga exitosa en {}ms: {} ({})", 
                            downloadTime, 
                            downloadResult.getFileName(),
                            downloadResult.getFormattedFileSize());
                } else {
                    failedDownloads++;
                    log.error("❌ Descarga fallida: {}", downloadResult.getErrorMessage());
                }
                
                // Pausa entre descargas para no saturar
                Thread.sleep(1000);
                
            } catch (Exception e) {
                failedDownloads++;
                log.error("❌ Error descargando con {}: {}", backendName, e.getMessage());
            }
        }
        
        return PerformanceResult.builder()
                .backendName(backendName)
                .totalSongs(TEST_SONGS.size())
                .successfulDownloads(successfulDownloads)
                .failedDownloads(failedDownloads)
                .totalTimeMs(totalTime)
                .averageTimeMs(successfulDownloads > 0 ? totalTime / successfulDownloads : 0)
                .totalFileSize(totalFileSize)
                .successRate(calculateSuccessRate(successfulDownloads, TEST_SONGS.size()))
                .testTimestamp(LocalDateTime.now())
                .build();
    }
    
    private void logPerformanceResult(PerformanceResult result) {
        log.info("📊 === RESULTADOS DE RENDIMIENTO: {} ===", result.getBackendName());
        log.info("🎵 Canciones totales: {}", result.getTotalSongs());
        log.info("✅ Descargas exitosas: {}", result.getSuccessfulDownloads());
        log.info("❌ Descargas fallidas: {}", result.getFailedDownloads());
        log.info("📈 Tasa de éxito: {}%", String.format("%.1f", result.getSuccessRate()));
        log.info("⏱️ Tiempo total: {}ms ({}s)", result.getTotalTimeMs(), String.format("%.1f", result.getTotalTimeMs() / 1000.0));
        log.info("⚡ Tiempo promedio por descarga: {}ms", result.getAverageTimeMs());
        log.info("💾 Tamaño total descargado: {}", formatFileSize(result.getTotalFileSize()));
        log.info("🚀 Velocidad promedio: {} MB/min", String.format("%.2f", result.getAverageSpeedMBPerMinute()));
        log.info("========================================");
    }
    
    private void displayFinalComparison(List<PerformanceResult> allResults) {
        if (allResults.isEmpty()) {
            log.warn("⚠️ No hay resultados para comparar");
            return;
        }
        
        log.info("🏆 === COMPARACIÓN FINAL DE BACKENDS ===");
        
        // Ordenar por velocidad promedio
        allResults.sort((a, b) -> Long.compare(a.getAverageTimeMs(), b.getAverageTimeMs()));
        
        for (int i = 0; i < allResults.size(); i++) {
            PerformanceResult result = allResults.get(i);
            String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "📊";
            
            log.info("{} {} - Promedio: {}ms, Éxito: {}%, Velocidad: {} MB/min", 
                    medal,
                    result.getBackendName(),
                    result.getAverageTimeMs(),
                    String.format("%.1f", result.getSuccessRate()),
                    String.format("%.2f", result.getAverageSpeedMBPerMinute()));
        }
        
        // Mostrar ganador
        if (!allResults.isEmpty()) {
            PerformanceResult winner = allResults.get(0);
            log.info("🏆 GANADOR: {} con {}ms promedio por descarga", 
                    winner.getBackendName(), winner.getAverageTimeMs());
        }
        
        log.info("==========================================");
    }
    
    private double calculateSuccessRate(int successful, int total) {
        if (total == 0) return 0.0;
        return (double) successful / total * 100.0;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        
        double size = bytes;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
    
    // Métodos de condición para JUnit
    static boolean isJTubeAvailable() {
        try {
            return DownloadBackend.JTUBE.isAvailable();
        } catch (Exception e) {
            return false;
        }
    }
    
    static boolean isYtDlpAvailable() {
        try {
            return DownloadBackend.YT_DLP.isAvailable();
        } catch (Exception e) {
            return false;
        }
    }
    
    // Clase para almacenar resultados de rendimiento
    @lombok.Builder
    @lombok.Data
    static class PerformanceResult {
        private String backendName;
        private int totalSongs;
        private int successfulDownloads;
        private int failedDownloads;
        private long totalTimeMs;
        private long averageTimeMs;
        private long totalFileSize;
        private double successRate;
        private LocalDateTime testTimestamp;
        
        public double getAverageSpeedMBPerMinute() {
            if (totalTimeMs == 0) return 0.0;
            double mbDownloaded = totalFileSize / (1024.0 * 1024.0);
            double minutesElapsed = totalTimeMs / (1000.0 * 60.0);
            return minutesElapsed > 0 ? mbDownloaded / minutesElapsed : 0.0;
        }
    }
}
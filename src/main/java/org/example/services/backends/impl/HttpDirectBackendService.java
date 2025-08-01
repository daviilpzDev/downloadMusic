package org.example.services.backends.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.models.DownloadRequest;
import org.example.models.DownloadResult;
import org.example.models.VideoInfo;
import org.example.services.backends.BackendMetrics;
import org.example.services.backends.DownloadBackendService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementación del backend HTTP Direct - Descarga HTTP directa
 */
@Slf4j
public class HttpDirectBackendService implements DownloadBackendService {
    
    private final AtomicLong downloadCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    public HttpDirectBackendService() {
        log.info("🚀 HTTP Direct Backend Service iniciado");
    }
    
    @Override
    public Optional<VideoInfo> searchVideo(String searchTerm) {
        log.warn("⚠️ HTTP Direct backend no soporta búsqueda");
        return Optional.empty();
    }
    
    @Override
    public List<VideoInfo> searchVideos(List<String> searchTerms) {
        log.warn("⚠️ HTTP Direct backend no soporta búsqueda");
        return List.of();
    }
    
    @Override
    public DownloadResult downloadAudio(DownloadRequest request) {
        log.warn("⚠️ HTTP Direct backend no implementado completamente");
        return DownloadResult.builder()
                .success(false)
                .errorMessage("HTTP Direct backend no implementado")
                .backendUsed("HTTP Direct")
                .build();
    }
    
    @Override
    public Optional<VideoInfo> getVideoInfo(String url) {
        log.warn("⚠️ HTTP Direct backend no implementado completamente");
        return Optional.empty();
    }
    
    @Override
    public boolean supportsUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
    
    @Override
    public String getBackendName() {
        return "HTTP Direct";
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Siempre disponible
    }
    
    @Override
    public BackendMetrics getMetrics() {
        return BackendMetrics.builder()
                .backendName("HTTP Direct")
                .totalDownloads(downloadCount.get())
                .successfulDownloads(successCount.get())
                .failedDownloads(errorCount.get())
                .successRate(0.0)
                .averageDownloadTimeMs(0L)
                .isAvailable(true)
                .build();
    }
}
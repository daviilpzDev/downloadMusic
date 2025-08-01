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
 * Implementación del backend PyTubeFix - Biblioteca Python via subprocess
 */
@Slf4j
public class PyTubeFixBackendService implements DownloadBackendService {
    
    private final AtomicLong downloadCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    public PyTubeFixBackendService() {
        log.info("🚀 PyTubeFix Backend Service iniciado");
    }
    
    @Override
    public Optional<VideoInfo> searchVideo(String searchTerm) {
        log.warn("⚠️ PyTubeFix backend no implementado completamente");
        return Optional.empty();
    }
    
    @Override
    public List<VideoInfo> searchVideos(List<String> searchTerms) {
        log.warn("⚠️ PyTubeFix backend no implementado completamente");
        return List.of();
    }
    
    @Override
    public DownloadResult downloadAudio(DownloadRequest request) {
        log.warn("⚠️ PyTubeFix backend no implementado completamente");
        return DownloadResult.builder()
                .success(false)
                .errorMessage("PyTubeFix backend no implementado")
                .backendUsed("PyTubeFix")
                .build();
    }
    
    @Override
    public Optional<VideoInfo> getVideoInfo(String url) {
        log.warn("⚠️ PyTubeFix backend no implementado completamente");
        return Optional.empty();
    }
    
    @Override
    public boolean supportsUrl(String url) {
        return url.contains("youtube.com") || url.contains("youtu.be");
    }
    
    @Override
    public String getBackendName() {
        return "PyTubeFix (Python Library)";
    }
    
    @Override
    public boolean isAvailable() {
        return false; // No implementado
    }
    
    @Override
    public BackendMetrics getMetrics() {
        return BackendMetrics.builder()
                .backendName("PyTubeFix")
                .totalDownloads(downloadCount.get())
                .successfulDownloads(successCount.get())
                .failedDownloads(errorCount.get())
                .successRate(0.0)
                .averageDownloadTimeMs(0L)
                .isAvailable(false)
                .build();
    }
}
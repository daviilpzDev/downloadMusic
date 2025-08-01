package org.example.services;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.DownloadBackend;
import org.example.models.DownloadRequest;
import org.example.models.DownloadResult;
import org.example.models.VideoInfo;
import org.example.services.backends.BackendFactory;
import org.example.services.backends.BackendMetrics;
import org.example.services.backends.DownloadBackendService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servicio avanzado de descarga que integra múltiples backends
 * y proporciona funcionalidades mejoradas como fallback automático,
 * métricas de rendimiento y descarga paralela
 */
@Slf4j
public class AdvancedDownloadService {
    
    private final ExecutorService executorService;
    private final List<DownloadBackendService> backends;
    private DownloadBackendService primaryBackend;
    
    public AdvancedDownloadService() {
        this.executorService = Executors.newFixedThreadPool(4);
        this.backends = new ArrayList<>();
        initializeBackends();
    }
    
    /**
     * Inicializa todos los backends disponibles
     */
    private void initializeBackends() {
        log.info("🚀 Inicializando backends de descarga...");
        
        for (DownloadBackend backendType : DownloadBackend.values()) {
            try {
                if (backendType.isAvailable()) {
                    DownloadBackendService backend = BackendFactory.createBackend(backendType);
                    backends.add(backend);
                    log.info("✅ Backend habilitado: {}", backend.getBackendName());
                } else {
                    log.warn("⚠️ Backend no disponible: {}", backendType.getDescription());
                }
            } catch (Exception e) {
                log.error("❌ Error inicializando backend {}: {}", 
                         backendType.getDescription(), e.getMessage());
            }
        }
        
        if (backends.isEmpty()) {
            throw new RuntimeException("❌ No hay backends disponibles para descarga");
        }
        
        // Establecer backend primario (el primero disponible por orden de preferencia)
        this.primaryBackend = backends.get(0);
        log.info("🎯 Backend primario: {}", primaryBackend.getBackendName());
    }
    
    /**
     * Busca y descarga múltiples canciones de forma inteligente
     */
    public List<DownloadResult> searchAndDownloadSongs(List<String> songQueries, String outputPath) {
        log.info("🎵 Iniciando descarga inteligente de {} canciones", songQueries.size());
        
        List<CompletableFuture<DownloadResult>> futures = songQueries.stream()
                .map(query -> searchAndDownloadSongAsync(query, outputPath))
                .toList();
        
        // Esperar a que todas las descargas terminen
        List<DownloadResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        // Estadísticas finales
        long successful = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        log.info("🎯 Descarga completada: {}/{} exitosas", successful, results.size());
        
        return results;
    }
    
    /**
     * Busca y descarga una canción de forma asíncrona
     */
    private CompletableFuture<DownloadResult> searchAndDownloadSongAsync(String query, String outputPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Buscar la canción
                Optional<VideoInfo> videoInfo = smartSearch(query);
                if (videoInfo.isEmpty()) {
                    return DownloadResult.builder()
                            .success(false)
                            .errorMessage("No se encontró la canción: " + query)
                            .build();
                }
                
                // 2. Crear request de descarga
                DownloadRequest request = DownloadRequest.builder()
                        .videoInfo(videoInfo.get())
                        .outputPath(outputPath)
                        .audioFormat(DownloadRequest.AudioFormat.MP3)
                        .audioQuality(DownloadRequest.AudioQuality.HIGH)
                        .overwriteExisting(false)
                        .build();
                
                // 3. Descargar con fallback automático
                return downloadWithFallback(request);
                
            } catch (Exception e) {
                log.error("❌ Error en descarga asíncrona de '{}': {}", query, e.getMessage());
                return DownloadResult.builder()
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        }, executorService);
    }
    
    /**
     * Búsqueda inteligente que prueba múltiples backends
     */
    private Optional<VideoInfo> smartSearch(String query) {
        log.info("🔍 Búsqueda inteligente: '{}'", query);
        
        // Primero intentar con el backend primario
        Optional<VideoInfo> result = primaryBackend.searchVideo(query);
        if (result.isPresent()) {
            log.info("✅ Encontrado con backend primario: {}", result.get().getTitle());
            return result;
        }
        
        // Si falla, intentar con otros backends
        for (DownloadBackendService backend : backends) {
            if (backend == primaryBackend) continue;
            
            try {
                result = backend.searchVideo(query);
                if (result.isPresent()) {
                    log.info("✅ Encontrado con backend alternativo {}: {}", 
                            backend.getBackendName(), result.get().getTitle());
                    return result;
                }
            } catch (Exception e) {
                log.warn("⚠️ Error en búsqueda con {}: {}", backend.getBackendName(), e.getMessage());
            }
        }
        
        log.warn("❌ No se encontró: '{}'", query);
        return Optional.empty();
    }
    
    /**
     * Descarga con fallback automático entre backends
     */
    private DownloadResult downloadWithFallback(DownloadRequest request) {
        log.info("📥 Descargando con fallback: '{}'", request.getVideoInfo().getTitle());
        
        // Intentar con el backend primario
        DownloadResult result = primaryBackend.downloadAudio(request);
        if (result.isSuccess()) {
            return result;
        }
        
        log.warn("⚠️ Fallo en backend primario, intentando alternativas...");
        
        // Intentar con backends alternativos
        for (DownloadBackendService backend : backends) {
            if (backend == primaryBackend) continue;
            
            try {
                log.info("🔄 Intentando con backend: {}", backend.getBackendName());
                result = backend.downloadAudio(request);
                
                if (result.isSuccess()) {
                    log.info("✅ Descarga exitosa con backend alternativo: {}", backend.getBackendName());
                    return result;
                }
            } catch (Exception e) {
                log.warn("⚠️ Error con backend {}: {}", backend.getBackendName(), e.getMessage());
            }
        }
        
        log.error("❌ Falló la descarga con todos los backends disponibles");
        return DownloadResult.builder()
                .success(false)
                .errorMessage("Todos los backends fallaron")
                .build();
    }
    
    /**
     * Obtiene métricas de todos los backends
     */
    public List<BackendMetrics> getAllBackendMetrics() {
        return backends.stream()
                .map(DownloadBackendService::getMetrics)
                .toList();
    }
    
    /**
     * Obtiene información de status del servicio
     */
    public ServiceStatus getServiceStatus() {
        return ServiceStatus.builder()
                .availableBackends(backends.size())
                .primaryBackend(primaryBackend.getBackendName())
                .allBackends(backends.stream().map(DownloadBackendService::getBackendName).toList())
                .metrics(getAllBackendMetrics())
                .build();
    }
    
    /**
     * Modelo para el status del servicio
     */
    @lombok.Builder
    @lombok.Data
    public static class ServiceStatus {
        private int availableBackends;
        private String primaryBackend;
        private List<String> allBackends;
        private List<BackendMetrics> metrics;
    }
    
    /**
     * Limpia recursos
     */
    public void shutdown() {
        log.info("🛑 Cerrando servicio de descarga avanzado...");
        executorService.shutdown();
    }
}
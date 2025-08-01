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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servicio avanzado de descarga que implementa fallback automático entre backends
 * Prioridad: JTube → yt-dlp → HTTP Direct
 */
@Slf4j
public class AdvancedDownloadService {
    
    // Orden de prioridad de backends (del más rápido al más lento)
    private static final DownloadBackend[] BACKEND_PRIORITY = {
        DownloadBackend.JTUBE,      // 1. Más rápido, Java nativo
        DownloadBackend.YT_DLP,     // 2. Robusto, herramienta externa
        DownloadBackend.HTTP_DIRECT // 3. Fallback final
    };
    
    private final List<DownloadBackendService> availableBackends;
    
    public AdvancedDownloadService() {
        this.availableBackends = initializeBackends();
        log.info("🚀 AdvancedDownloadService iniciado con {} backends disponibles", availableBackends.size());
    }
    
    /**
     * Busca y descarga una canción usando fallback automático
     */
    public DownloadResult searchAndDownload(String searchTerm, String outputPath) {
        log.info("🔍 Iniciando búsqueda y descarga: '{}'", searchTerm);
        
        if (availableBackends.isEmpty()) {
            log.error("❌ No hay backends disponibles");
            return DownloadResult.builder()
                    .success(false)
                    .errorMessage("No hay backends disponibles")
                    .build();
        }
        
        // Intentar con cada backend en orden de prioridad
        for (int i = 0; i < availableBackends.size(); i++) {
            DownloadBackendService backend = availableBackends.get(i);
            String backendName = backend.getBackendName();
            
            log.info("🔄 Intentando con backend {} ({}/{}): {}", 
                    backendName, i + 1, availableBackends.size(), backendName);
            
            try {
                // Paso 1: Buscar información del video
                Optional<VideoInfo> videoInfoOpt = backend.searchVideo(searchTerm);
                
                if (videoInfoOpt.isEmpty()) {
                    log.warn("⚠️ Backend {} no encontró información para: '{}'", backendName, searchTerm);
                    continue; // Probar siguiente backend
                }
                
                VideoInfo videoInfo = videoInfoOpt.get();
                log.info("✅ Backend {} encontró: '{}'", backendName, videoInfo.getTitle());
                
                // Paso 2: Intentar descarga
                DownloadRequest request = DownloadRequest.builder()
                        .videoInfo(videoInfo)
                        .outputPath(outputPath)
                        .audioFormat(DownloadRequest.AudioFormat.MP3)
                        .audioQuality(DownloadRequest.AudioQuality.MEDIUM)
                        .overwriteExisting(true)
                        .build();
                
                DownloadResult result = backend.downloadAudio(request);
                
                if (result.isSuccess()) {
                    log.info("🎉 Descarga exitosa con backend {}: {}", backendName, result.getFileName());
                    return result;
                } else {
                    log.warn("⚠️ Backend {} falló en descarga: {}", backendName, result.getErrorMessage());
                    // Continuar con siguiente backend
                }
                
            } catch (Exception e) {
                log.error("❌ Error con backend {}: {}", backendName, e.getMessage());
                // Continuar con siguiente backend
            }
        }
        
        // Si llegamos aquí, todos los backends fallaron
        log.error("❌ Todos los backends fallaron para: '{}'", searchTerm);
        return DownloadResult.builder()
                .success(false)
                .errorMessage("Todos los backends fallaron")
                .build();
    }
    
    /**
     * Descarga múltiples canciones con fallback automático
     */
    public List<DownloadResult> searchAndDownloadMultiple(List<String> searchTerms, String outputPath) {
        log.info("🎵 Iniciando descarga múltiple: {} canciones", searchTerms.size());
        
        List<DownloadResult> results = new ArrayList<>();
        
        for (int i = 0; i < searchTerms.size(); i++) {
            String searchTerm = searchTerms.get(i);
            log.info("🔄 Procesando canción {}/{}: '{}'", i + 1, searchTerms.size(), searchTerm);
            
            DownloadResult result = searchAndDownload(searchTerm, outputPath);
            results.add(result);
            
            // Mostrar progreso
            if (result.isSuccess()) {
                log.info("✅ {}/{} completada: {}", i + 1, searchTerms.size(), result.getFileName());
            } else {
                log.error("❌ {}/{} falló: {}", i + 1, searchTerms.size(), searchTerm);
            }
        }
        
        // Resumen final
        long successful = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        log.info("📊 Resumen: {}/{} descargas exitosas", successful, results.size());
        
        return results;
    }
    
    /**
     * Obtiene información de video con fallback automático
     */
    public Optional<VideoInfo> getVideoInfo(String searchTerm) {
        log.info("📋 Obteniendo información de: '{}'", searchTerm);
        
        for (DownloadBackendService backend : availableBackends) {
            try {
                Optional<VideoInfo> result = backend.searchVideo(searchTerm);
                if (result.isPresent()) {
                    log.info("✅ Información obtenida con {}: '{}'", 
                            backend.getBackendName(), result.get().getTitle());
                    return result;
                }
            } catch (Exception e) {
                log.debug("Backend {} falló: {}", backend.getBackendName(), e.getMessage());
            }
        }
        
        log.warn("⚠️ No se pudo obtener información para: '{}'", searchTerm);
        return Optional.empty();
    }
    
    /**
     * Obtiene métricas de todos los backends
     */
    public List<BackendMetrics> getAllBackendMetrics() {
        return availableBackends.stream()
                .map(DownloadBackendService::getMetrics)
                .toList();
    }
    
    /**
     * Obtiene el estado de disponibilidad de todos los backends
     */
    public List<BackendStatus> getBackendStatus() {
        List<BackendStatus> statusList = new ArrayList<>();
        
        for (DownloadBackend backendType : BACKEND_PRIORITY) {
            boolean available = backendType.isAvailable();
            statusList.add(new BackendStatus(
                    backendType.getDescription(),
                    available,
                    available ? "Disponible" : "No disponible"
            ));
        }
        
        return statusList;
    }
    
    /**
     * Inicializa los backends disponibles en orden de prioridad
     */
    private List<DownloadBackendService> initializeBackends() {
        List<DownloadBackendService> backends = new ArrayList<>();
        
        for (DownloadBackend backendType : BACKEND_PRIORITY) {
            if (backendType.isAvailable()) {
                try {
                    DownloadBackendService backend = BackendFactory.createBackend(backendType);
                    backends.add(backend);
                    log.info("✅ Backend habilitado: {} ({})", 
                            backendType.getDescription(), backend.getBackendName());
                } catch (Exception e) {
                    log.warn("⚠️ Error inicializando backend {}: {}", 
                            backendType.getDescription(), e.getMessage());
                }
            } else {
                log.warn("❌ Backend no disponible: {}", backendType.getDescription());
            }
        }
        
        if (backends.isEmpty()) {
            log.error("❌ ¡CRÍTICO! No hay backends disponibles");
        } else {
            log.info("🎯 Sistema de fallback configurado con {} backends", backends.size());
        }
        
        return backends;
    }
    
    /**
     * Clase para estado de backend
     */
    public static class BackendStatus {
        public final String name;
        public final boolean available;
        public final String status;
        
        public BackendStatus(String name, boolean available, String status) {
            this.name = name;
            this.available = available;
            this.status = status;
        }
        
        @Override
        public String toString() {
            String icon = available ? "✅" : "❌";
            return String.format("%s %s: %s", icon, name, status);
        }
    }
}
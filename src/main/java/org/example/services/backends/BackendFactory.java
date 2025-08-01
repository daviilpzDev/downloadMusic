package org.example.services.backends;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.DownloadBackend;
import org.example.services.backends.impl.*;

/**
 * Factory para crear instancias de diferentes backends de descarga
 */
@Slf4j
public class BackendFactory {
    
    /**
     * Crea una instancia del backend especificado
     */
    public static DownloadBackendService createBackend(DownloadBackend backend) {
        log.info("🏭 Creando backend: {}", backend.getDescription());
        
        return switch (backend) {
            case YT_DLP -> new YtDlpBackendService();
            case JTUBE -> new JTubeBackendService();
            case PYTUBEFIX -> new PyTubeFixBackendService();
            case HTTP_DIRECT -> new HttpDirectBackendService();
        };
    }
    
    /**
     * Obtiene el mejor backend disponible en el sistema
     */
    public static DownloadBackendService getBestAvailableBackend() {
        log.info("🔍 Buscando el mejor backend disponible...");
        
        // Orden de preferencia
        DownloadBackend[] preferenceOrder = {
            DownloadBackend.JTUBE,      // Más rápido (Java nativo)
            DownloadBackend.YT_DLP,     // Más completo
            DownloadBackend.PYTUBEFIX,  // Alternativa Python
            DownloadBackend.HTTP_DIRECT // Fallback básico
        };
        
        for (DownloadBackend backend : preferenceOrder) {
            if (backend.isAvailable()) {
                log.info("✅ Backend seleccionado: {}", backend.getDescription());
                return createBackend(backend);
            } else {
                log.warn("❌ Backend no disponible: {}", backend.getDescription());
            }
        }
        
        throw new RuntimeException("❌ No hay backends de descarga disponibles en el sistema");
    }
    
    /**
     * Obtiene todos los backends disponibles
     */
    public static java.util.List<DownloadBackend> getAvailableBackends() {
        return java.util.Arrays.stream(DownloadBackend.values())
                .filter(DownloadBackend::isAvailable)
                .toList();
    }
}
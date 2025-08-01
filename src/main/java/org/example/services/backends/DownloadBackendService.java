package org.example.services.backends;

import org.example.models.DownloadRequest;
import org.example.models.DownloadResult;
import org.example.models.VideoInfo;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz base para todos los backends de descarga
 */
public interface DownloadBackendService {
    
    /**
     * Busca información de un video/audio por término de búsqueda
     */
    Optional<VideoInfo> searchVideo(String searchTerm);
    
    /**
     * Busca múltiples videos/audios
     */
    List<VideoInfo> searchVideos(List<String> searchTerms);
    
    /**
     * Descarga un archivo basado en la información del video
     */
    DownloadResult downloadAudio(DownloadRequest request);
    
    /**
     * Obtiene información detallada de un video por URL
     */
    Optional<VideoInfo> getVideoInfo(String url);
    
    /**
     * Verifica si el backend soporta la URL dada
     */
    boolean supportsUrl(String url);
    
    /**
     * Obtiene el nombre del backend
     */
    String getBackendName();
    
    /**
     * Verifica si el backend está disponible
     */
    boolean isAvailable();
    
    /**
     * Obtiene métricas de rendimiento del backend
     */
    BackendMetrics getMetrics();
}
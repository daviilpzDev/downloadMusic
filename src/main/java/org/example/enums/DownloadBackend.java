package org.example.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum que define los diferentes backends de descarga disponibles
 */
@Getter
@RequiredArgsConstructor
public enum DownloadBackend {
    
    /**
     * yt-dlp - El backend tradicional (herramienta externa)
     */
    YT_DLP("yt-dlp", "Herramienta externa yt-dlp", true, true),
    
    /**
     * JTube - Biblioteca Java nativa para YouTube
     */
    JTUBE("jtube", "Biblioteca Java nativa", false, false),
    
    /**
     * PyTubeFix - Biblioteca Python via subprocess
     */
    PYTUBEFIX("pytubefix", "Biblioteca Python pytube-fix", true, false),
    
    /**
     * HTTP Direct - Descarga directa via HTTP
     */
    HTTP_DIRECT("http-direct", "Descarga HTTP directa", false, true);
    
    private final String name;
    private final String description;
    private final boolean requiresExternalTool;
    private final boolean supportsSearch;
    
    /**
     * Verifica si este backend está disponible en el sistema actual
     */
    public boolean isAvailable() {
        return switch (this) {
            case YT_DLP -> isYtDlpAvailable();
            case JTUBE -> true; // Siempre disponible en modo simulación
            case PYTUBEFIX -> false; // No implementado
            case HTTP_DIRECT -> true; // Siempre disponible
        };
    }
    
    /**
     * Verifica si yt-dlp está disponible
     */
    private boolean isYtDlpAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("yt-dlp", "--version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Obtiene todos los backends disponibles
     */
    public static DownloadBackend[] getAvailableBackends() {
        return java.util.Arrays.stream(values())
                .filter(DownloadBackend::isAvailable)
                .toArray(DownloadBackend[]::new);
    }
    
    /**
     * Obtiene el backend por defecto (el primero disponible)
     */
    public static DownloadBackend getDefault() {
        for (DownloadBackend backend : values()) {
            if (backend.isAvailable()) {
                return backend;
            }
        }
        return YT_DLP; // Fallback
    }
}
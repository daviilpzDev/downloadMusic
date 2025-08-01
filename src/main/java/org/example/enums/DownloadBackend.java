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
    HTTP_DIRECT("http-direct", "Descarga HTTP directa", false, false);
    
    private final String id;
    private final String description;
    private final boolean requiresExternalTool;
    private final boolean supportsAllPlatforms;
    
    /**
     * Obtiene un backend por su ID
     */
    public static DownloadBackend fromId(String id) {
        for (DownloadBackend backend : values()) {
            if (backend.getId().equalsIgnoreCase(id)) {
                return backend;
            }
        }
        throw new IllegalArgumentException("Backend no encontrado: " + id);
    }
    
    /**
     * Verifica si el backend está disponible en el sistema
     */
    public boolean isAvailable() {
        switch (this) {
            case YT_DLP:
                return checkYtDlpAvailable();
            case JTUBE:
                return true; // Siempre disponible (biblioteca Java)
            case PYTUBEFIX:
                return checkPythonAvailable();
            case HTTP_DIRECT:
                return true; // Siempre disponible
            default:
                return false;
        }
    }
    
    private boolean checkYtDlpAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("yt-dlp", "--version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean checkPythonAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "-c", "import pytubefix");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
package org.example.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

/**
 * Resultado de una operación de descarga
 */
@Data
@Builder
@Jacksonized
public class DownloadResult {
    
    private boolean success;
    private String filePath;
    private String fileName;
    private long fileSize;
    private String backendUsed;
    private Long downloadTimeMs;
    private String errorMessage;
    private LocalDateTime downloadedAt;
    private DownloadMetadata metadata;
    
    /**
     * Metadatos adicionales de la descarga
     */
    @Data
    @Builder
    @Jacksonized
    public static class DownloadMetadata {
        private String originalUrl;
        private String videoTitle;
        private String videoId;
        private String videoDescription;
        private String channelName;
        private String audioQuality;
        private String audioFormat;
    }
    
    /**
     * Obtiene el tiempo de descarga formateado
     */
    public String getFormattedDownloadTime() {
        if (downloadTimeMs == null || downloadTimeMs == 0) {
            return "N/A";
        }
        
        if (downloadTimeMs < 1000) {
            return downloadTimeMs + "ms";
        } else if (downloadTimeMs < 60000) {
            return String.format("%.1fs", downloadTimeMs / 1000.0);
        } else {
            long minutes = downloadTimeMs / 60000;
            long seconds = (downloadTimeMs % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    /**
     * Obtiene el tamaño del archivo formateado
     */
    public String getFormattedFileSize() {
        if (fileSize <= 0) return "0 B";
        
        double size = fileSize;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
    
    /**
     * Verifica si la descarga fue exitosa
     */
    public boolean isSuccessful() {
        return success;
    }
    
    /**
     * Obtiene un resumen textual del resultado
     */
    public String getSummary() {
        if (success) {
            return String.format("✅ Descarga exitosa: %s (%s) en %s", 
                    fileName, getFormattedFileSize(), getFormattedDownloadTime());
        } else {
            return String.format("❌ Descarga fallida: %s", 
                    errorMessage != null ? errorMessage : "Error desconocido");
        }
    }
}
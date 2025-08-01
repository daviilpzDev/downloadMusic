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
        private Long videoDurationSeconds;
        private String audioFormat;
        private Integer audioBitrate;
        private Long originalFileSize;
    }
    
    /**
     * Obtiene el tamaño del archivo en formato legible
     */
    public String getFormattedFileSize() {
        if (fileSize <= 0) return "Unknown";
        
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
     * Obtiene el tiempo de descarga en formato legible
     */
    public String getFormattedDownloadTime() {
        if (downloadTimeMs == null || downloadTimeMs <= 0) return "Unknown";
        
        long seconds = downloadTimeMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Verifica si la descarga fue exitosa y el archivo existe
     */
    public boolean isValid() {
        return success && 
               filePath != null && 
               !filePath.isEmpty() && 
               new java.io.File(filePath).exists();
    }
}
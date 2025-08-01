package org.example.services.backends;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

/**
 * Métricas de rendimiento de un backend de descarga
 */
@Data
@Builder
@Jacksonized
public class BackendMetrics {
    
    private String backendName;
    private long totalDownloads;
    private long successfulDownloads;
    private long failedDownloads;
    private double successRate;
    private long averageDownloadTimeMs;
    private boolean isAvailable;
    private LocalDateTime lastUsed;
    private String version;
    
    /**
     * Obtiene la tasa de éxito como porcentaje formateado
     */
    public String getFormattedSuccessRate() {
        return String.format("%.1f%%", successRate);
    }
    
    /**
     * Obtiene el tiempo promedio de descarga en formato legible
     */
    public String getFormattedAverageTime() {
        if (averageDownloadTimeMs <= 0) return "N/A";
        
        long seconds = averageDownloadTimeMs / 1000;
        if (seconds < 60) {
            return seconds + "s";
        } else {
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    /**
     * Obtiene el estado del backend como emoji
     */
    public String getStatusEmoji() {
        if (!isAvailable) return "❌";
        if (successRate >= 95) return "🟢";
        if (successRate >= 80) return "🟡";
        return "🔴";
    }
    
    /**
     * Obtiene un resumen textual de las métricas
     */
    public String getSummary() {
        return String.format("%s %s: %d downloads, %s success rate, %s avg time", 
                getStatusEmoji(), 
                backendName, 
                totalDownloads, 
                getFormattedSuccessRate(),
                getFormattedAverageTime());
    }
}
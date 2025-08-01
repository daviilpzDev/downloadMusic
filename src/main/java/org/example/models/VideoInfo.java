package org.example.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;
import java.util.List;

/**
 * Información detallada de un video/audio
 */
@Data
@Builder
@Jacksonized
public class VideoInfo {
    
    private String id;
    private String title;
    private String artist;
    private String url;
    private String thumbnailUrl;
    private Duration duration;
    private String description;
    private List<String> tags;
    private VideoQuality quality;
    private long viewCount;
    private String uploader;
    private java.time.LocalDateTime uploadDate;
    
    /**
     * Calidad del video/audio
     */
    public enum VideoQuality {
        LOW, MEDIUM, HIGH, ULTRA_HIGH
    }
    
    /**
     * Obtiene el título sanitizado para nombre de archivo
     */
    public String getSanitizedTitle() {
        if (title == null) return "unknown";
        return title.replaceAll("[^a-zA-Z0-9\\s\\-_]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
    
    /**
     * Obtiene el nombre de archivo sugerido
     */
    public String getSuggestedFilename() {
        String sanitized = getSanitizedTitle();
        if (artist != null && !artist.isEmpty()) {
            return artist + " - " + sanitized + ".mp3";
        }
        return sanitized + ".mp3";
    }
}
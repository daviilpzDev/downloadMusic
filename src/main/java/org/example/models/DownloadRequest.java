package org.example.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Modelo para peticiones de descarga
 */
@Data
@Builder
@Jacksonized
public class DownloadRequest {
    
    private VideoInfo videoInfo;
    private String outputPath;
    private String customFilename;
    private AudioFormat audioFormat;
    private AudioQuality audioQuality;
    private boolean overwriteExisting;
    
    /**
     * Formatos de audio soportados
     */
    public enum AudioFormat {
        MP3("mp3"), 
        M4A("m4a"), 
        FLAC("flac"), 
        OGG("ogg"),
        WAV("wav");
        
        private final String extension;
        
        AudioFormat(String extension) {
            this.extension = extension;
        }
        
        public String getExtension() {
            return extension;
        }
    }
    
    /**
     * Calidades de audio
     */
    public enum AudioQuality {
        LOW("128K"),
        MEDIUM("192K"), 
        HIGH("256K"),
        ULTRA_HIGH("320K");
        
        private final String bitrate;
        
        AudioQuality(String bitrate) {
            this.bitrate = bitrate;
        }
        
        public String getBitrate() {
            return bitrate;
        }
    }
    
    /**
     * Obtiene el nombre de archivo objetivo basado en la configuración
     */
    public String getTargetFilename() {
        if (customFilename != null && !customFilename.trim().isEmpty()) {
            // Usar nombre personalizado
            String filename = customFilename.trim();
            if (!filename.toLowerCase().endsWith("." + audioFormat.getExtension())) {
                filename += "." + audioFormat.getExtension();
            }
            return sanitizeFilename(filename);
        } else if (videoInfo != null) {
            // Usar título del video
            String title = videoInfo.getTitle();
            if (title == null || title.trim().isEmpty()) {
                title = "Unknown_" + System.currentTimeMillis();
            }
            return sanitizeFilename(title) + "." + audioFormat.getExtension();
        } else {
            // Fallback
            return "download_" + System.currentTimeMillis() + "." + audioFormat.getExtension();
        }
    }
    
    /**
     * Sanitiza el nombre de archivo eliminando caracteres problemáticos
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_")
                      .replaceAll("\\s+", "_")
                      .replaceAll("_+", "_")
                      .trim();
    }
}
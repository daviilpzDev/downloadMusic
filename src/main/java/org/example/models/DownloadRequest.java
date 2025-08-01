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
     * Calidades de audio disponibles
     */
    public enum AudioQuality {
        LOW(96), 
        MEDIUM(128), 
        HIGH(192), 
        ULTRA_HIGH(320);
        
        private final int bitrate;
        
        AudioQuality(int bitrate) {
            this.bitrate = bitrate;
        }
        
        public int getBitrate() {
            return bitrate;
        }
    }
    
    /**
     * Obtiene el nombre de archivo final
     */
    public String getTargetFilename() {
        if (customFilename != null && !customFilename.isEmpty()) {
            return customFilename;
        }
        
        String baseFilename = videoInfo.getSanitizedTitle();
        AudioFormat format = audioFormat != null ? audioFormat : AudioFormat.MP3;
        
        return baseFilename + "." + format.getExtension();
    }
}
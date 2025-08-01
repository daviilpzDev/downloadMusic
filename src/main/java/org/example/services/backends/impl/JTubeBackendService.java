package org.example.services.backends.impl;

import io.github.x45iq.jtube.parsers.VideoParser;
import io.github.x45iq.jtube.streamingdata.StreamingData;
import io.github.x45iq.jtube.streamingdata.StreamingDataDownloader;
import io.github.x45iq.jtube.models.Video;
import lombok.extern.slf4j.Slf4j;
import org.example.models.DownloadRequest;
import org.example.models.DownloadResult;
import org.example.models.VideoInfo;
import org.example.services.backends.BackendMetrics;
import org.example.services.backends.DownloadBackendService;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementación del backend JTube - Biblioteca Java nativa para YouTube
 * Esta es una alternativa moderna a yt-dlp que no requiere herramientas externas
 */
@Slf4j
public class JTubeBackendService implements DownloadBackendService {
    
    private final VideoParser videoParser;
    private final AtomicLong downloadCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    
    public JTubeBackendService() {
        this.videoParser = new VideoParser();
        log.info("🚀 JTube Backend Service iniciado");
    }
    
    @Override
    public Optional<VideoInfo> searchVideo(String searchTerm) {
        log.info("🔍 Buscando video con JTube: '{}'", searchTerm);
        
        try {
            // Construir URL de búsqueda de YouTube
            String searchUrl = "https://www.youtube.com/results?search_query=" + 
                             searchTerm.replace(" ", "+");
            
            // Para búsquedas necesitaríamos implementar parsing de resultados
            // Por ahora, intentamos si el término es una URL
            if (searchTerm.contains("youtube.com") || searchTerm.contains("youtu.be")) {
                return getVideoInfo(searchTerm);
            }
            
            // TODO: Implementar búsqueda real por términos
            log.warn("⚠️ JTube no soporta búsqueda por términos aún, solo URLs directas");
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("❌ Error buscando video con JTube: {}", e.getMessage());
            errorCount.incrementAndGet();
            return Optional.empty();
        }
    }
    
    @Override
    public List<VideoInfo> searchVideos(List<String> searchTerms) {
        log.info("🔍 Buscando {} videos con JTube", searchTerms.size());
        
        return searchTerms.parallelStream()
                .map(this::searchVideo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
    
    @Override
    public DownloadResult downloadAudio(DownloadRequest request) {
        log.info("📥 Descargando audio con JTube: '{}'", request.getVideoInfo().getTitle());
        downloadCount.incrementAndGet();
        
        try {
            // Parsear el video desde la URL
            Video video = videoParser.parse(request.getVideoInfo().getUrl());
            
            // Buscar el stream de audio de mejor calidad
            StreamingData audioStream = video.streamingData().stream()
                    .filter(stream -> stream.mimeType().contains("audio"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró stream de audio"));
            
            // Configurar descarga
            File outputDir = new File(request.getOutputPath());
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Descargar archivo
            File downloadedFile = new StreamingDataDownloader.Builder()
                    .streamingData(audioStream)
                    .folder(outputDir)
                    .progressCallback(progress -> 
                        log.debug("📊 Progreso JTube: {}%", progress))
                    .build()
                    .download();
            
            // Renombrar al nombre deseado
            String targetFilename = request.getVideoInfo().getSuggestedFilename();
            File targetFile = new File(outputDir, targetFilename);
            
            if (downloadedFile.renameTo(targetFile)) {
                log.info("✅ Descarga JTube completada: {}", targetFile.getName());
                successCount.incrementAndGet();
                
                return DownloadResult.builder()
                        .success(true)
                        .filePath(targetFile.getAbsolutePath())
                        .fileName(targetFile.getName())
                        .fileSize(targetFile.length())
                        .backendUsed("JTube")
                        .downloadTimeMs(0L) // TODO: Medir tiempo real
                        .build();
            } else {
                throw new RuntimeException("No se pudo renombrar el archivo descargado");
            }
            
        } catch (Exception e) {
            log.error("❌ Error descargando con JTube: {}", e.getMessage());
            errorCount.incrementAndGet();
            
            return DownloadResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .backendUsed("JTube")
                    .build();
        }
    }
    
    @Override
    public Optional<VideoInfo> getVideoInfo(String url) {
        log.info("📋 Obteniendo info de video con JTube: {}", url);
        
        try {
            if (!VideoParser.isUrlSupported(url)) {
                log.warn("⚠️ URL no soportada por JTube: {}", url);
                return Optional.empty();
            }
            
            Video video = videoParser.parse(url);
            
            VideoInfo videoInfo = VideoInfo.builder()
                    .id(video.id())
                    .title(video.title())
                    .url(url)
                    .description(video.description())
                    .duration(Duration.ofSeconds(video.duration()))
                    .viewCount(video.viewCount())
                    .uploader(video.uploader())
                    .uploadDate(LocalDateTime.now()) // JTube no proporciona fecha exacta
                    .quality(VideoInfo.VideoQuality.HIGH)
                    .thumbnailUrl(video.thumbnails().isEmpty() ? null : 
                                video.thumbnails().get(0).url())
                    .build();
            
            log.info("✅ Info obtenida con JTube: '{}'", videoInfo.getTitle());
            return Optional.of(videoInfo);
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo info con JTube: {}", e.getMessage());
            errorCount.incrementAndGet();
            return Optional.empty();
        }
    }
    
    @Override
    public boolean supportsUrl(String url) {
        return VideoParser.isUrlSupported(url);
    }
    
    @Override
    public String getBackendName() {
        return "JTube (Java Native)";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Verificar que la biblioteca JTube esté disponible
            new VideoParser();
            return true;
        } catch (Exception e) {
            log.error("❌ JTube no está disponible: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public BackendMetrics getMetrics() {
        return BackendMetrics.builder()
                .backendName("JTube")
                .totalDownloads(downloadCount.get())
                .successfulDownloads(successCount.get())
                .failedDownloads(errorCount.get())
                .successRate(calculateSuccessRate())
                .averageDownloadTimeMs(0L) // TODO: Implementar medición de tiempo
                .isAvailable(isAvailable())
                .build();
    }
    
    private double calculateSuccessRate() {
        long total = downloadCount.get();
        if (total == 0) return 0.0;
        return (double) successCount.get() / total * 100.0;
    }
}
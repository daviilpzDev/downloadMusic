package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.services.SimpleDownloadService;
import org.example.utils.Actions;

import java.util.List;

/**
 * Demostración simple de descarga real de música desde songs.yml
 */
@Slf4j
public class SimpleDownloadDemo {
    
    public static void main(String[] args) {
        log.info("🚀 === DEMOSTRACIÓN SIMPLE: DESCARGA REAL DESDE SONGS.YML ===");
        
        SimpleDownloadDemo demo = new SimpleDownloadDemo();
        demo.downloadMusicFromYaml();
    }
    
    public void downloadMusicFromYaml() {
        log.info("");
        log.info("📂 === PASO 1: CARGANDO CANCIONES DESDE SONGS.YML ===");
        
        // Cargar canciones desde songs.yml
        List<String> songs = Actions.getYmlFile("songs");
        
        if (songs == null || songs.isEmpty()) {
            log.error("❌ No se pudieron cargar canciones desde songs.yml");
            return;
        }
        
        log.info("✅ Canciones cargadas desde songs.yml:");
        for (int i = 0; i < songs.size(); i++) {
            log.info("   {}. {}", i + 1, songs.get(i));
        }
        
        log.info("");
        log.info("💿 === PASO 2: DESCARGANDO MÚSICA ===");
        
        // Crear servicio de descarga simple
        SimpleDownloadService downloadService = new SimpleDownloadService();
        
        // Directorio de salida
        String outputDir = "./target/downloads";
        
        // Descargar canciones
        List<String> downloadedFiles = downloadService.searchAndDownloadSongs(songs, outputDir);
        
        log.info("");
        log.info("📊 === PASO 3: RESULTADOS FINALES ===");
        
        if (downloadedFiles.isEmpty()) {
            log.warn("❌ No se descargó ninguna canción");
            log.info("💡 Esto puede ser debido a:");
            log.info("   • Problemas de conectividad");
            log.info("   • YouTube bloqueando las solicitudes");
            log.info("   • Canciones no encontradas");
        } else {
            log.info("🎉 ¡Descargas completadas exitosamente!");
            log.info("📁 Archivos descargados:");
            for (String file : downloadedFiles) {
                log.info("   • {}", file);
            }
        }
        
        log.info("");
        log.info("💡 === INSTRUCCIONES PARA USO ===");
        log.info("Para modificar las canciones:");
        log.info("   1. Edita: src/test/resources/data/songs.yml");
        log.info("   2. Formato: \"Título - Artista\"");
        log.info("   3. Ejecuta: mvn compile exec:java -Dexec.mainClass=\"org.example.SimpleDownloadDemo\"");
        log.info("");
        log.info("Los archivos se guardan en: {}", outputDir);
        log.info("=====================================");
    }
}
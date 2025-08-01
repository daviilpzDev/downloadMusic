package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.services.RealDownloadService;

import java.io.File;
import java.util.List;

/**
 * Demostración real de descarga de música desde songs.yml
 */
@Slf4j
public class RealDownloadDemo {
    
    public static void main(String[] args) {
        log.info("🚀 === DEMOSTRACIÓN: DESCARGA REAL DE MÚSICA ===");
        
        RealDownloadDemo demo = new RealDownloadDemo();
        demo.runRealDownload();
    }
    
    public void runRealDownload() {
        log.info("");
        log.info("🎯 Objetivo: Descargar canciones reales desde songs.yml");
        log.info("");
        
        // Crear servicio de descarga real
        String outputDir = "./target/real-downloads";
        RealDownloadService downloadService = new RealDownloadService(outputDir);
        
        log.info("📁 Directorio de descarga: {}", outputDir);
        
        // Descargar canciones desde YAML
        List<String> downloadedFiles = downloadService.downloadSongsFromYaml();
        
        // Mostrar resultados
        showResults(downloadedFiles, outputDir);
    }
    
    private void showResults(List<String> downloadedFiles, String outputDir) {
        log.info("");
        log.info("🎉 === RESULTADOS FINALES ===");
        
        if (downloadedFiles.isEmpty()) {
            log.warn("❌ No se descargó ningún archivo");
            log.info("💡 Posibles causas:");
            log.info("   • YouTube bloquea las descargas (bot detection)");
            log.info("   • Problemas de conectividad");
            log.info("   • Canciones no encontradas");
            log.info("");
            log.info("🔧 Soluciones sugeridas:");
            log.info("   • Usar canciones más genéricas (ej: 'music', 'song')");
            log.info("   • Probar con un VPN");
            log.info("   • Usar el servicio original simple");
        } else {
            log.info("✅ ¡Descarga exitosa! Archivos generados:");
            for (String file : downloadedFiles) {
                File f = new File(file);
                if (f.exists()) {
                    long sizeKB = f.length() / 1024;
                    log.info("   📄 {} ({} KB)", f.getName(), sizeKB);
                }
            }
            
            log.info("");
            log.info("📂 Archivos guardados en: {}", outputDir);
        }
        
        log.info("");
        log.info("💡 === COMANDOS ÚTILES ===");
        log.info("Para ver archivos descargados:");
        log.info("   ls -la {}", outputDir);
        log.info("");
        log.info("Para probar con otras canciones:");
        log.info("   1. Edita: src/test/resources/data/songs.yml");
        log.info("   2. Ejecuta: mvn compile exec:java -Dexec.mainClass=\"org.example.RealDownloadDemo\"");
        log.info("");
        log.info("=====================================");
    }
}
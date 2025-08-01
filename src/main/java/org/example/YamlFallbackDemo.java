package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.models.DownloadResult;
import org.example.services.AdvancedDownloadService;
import org.example.services.AdvancedDownloadService.BackendStatus;
import org.example.utils.Actions;

import java.util.List;

/**
 * Demostración del sistema de fallback automático usando canciones desde songs.yml
 */
@Slf4j
public class YamlFallbackDemo {
    
    public static void main(String[] args) {
        log.info("🚀 === DEMOSTRACIÓN: FALLBACK CON CANCIONES DESDE SONGS.YML ===");
        
        YamlFallbackDemo demo = new YamlFallbackDemo();
        demo.demonstrateYamlFallback();
    }
    
    public void demonstrateYamlFallback() {
        log.info("");
        log.info("📂 === PASO 1: CARGANDO CANCIONES DESDE SONGS.YML ===");
        
        List<String> songsFromYaml;
        try {
            // Cargar canciones usando la utilidad existente
            songsFromYaml = Actions.getYmlFile("songs");
            
            log.info("✅ Canciones cargadas exitosamente desde songs.yml:");
            for (int i = 0; i < songsFromYaml.size(); i++) {
                log.info("   {}. {} ", i + 1, songsFromYaml.get(i));
            }
            
        } catch (Exception e) {
            log.error("❌ Error cargando songs.yml: {}", e.getMessage());
            return;
        }
        
        log.info("");
        log.info("🔧 === PASO 2: INICIALIZANDO SERVICIO CON FALLBACK ===");
        
        // Crear servicio con fallback automático
        AdvancedDownloadService downloadService = new AdvancedDownloadService();
        
        // Mostrar estado inicial de backends
        showBackendStatus(downloadService);
        
        log.info("");
        log.info("🎵 === PASO 3: DESCARGA CON FALLBACK DESDE YAML ===");
        
        // Probar descarga múltiple con fallback usando canciones del YAML
        log.info("🔄 Iniciando descarga con canciones desde songs.yml...");
        List<DownloadResult> results = downloadService.searchAndDownloadMultiple(
                songsFromYaml, "./target/yaml-fallback-demo/");
        
        // Mostrar resultados detallados
        showDownloadResults(results, songsFromYaml);
        
        log.info("");
        log.info("🧪 === PASO 4: PRUEBA INDIVIDUAL DESDE YAML ===");
        
        if (!songsFromYaml.isEmpty()) {
            String firstSong = songsFromYaml.get(0);
            log.info("🔄 Probando descarga individual: '{}'", firstSong);
            
            DownloadResult result = downloadService.searchAndDownload(
                    firstSong, "./target/yaml-fallback-demo/");
            
            if (result.isSuccess()) {
                log.info("✅ Descarga individual exitosa: {} ({})", 
                        result.getFileName(), result.getFormattedFileSize());
            } else {
                log.info("❌ Descarga individual falló: {}", result.getErrorMessage());
            }
        }
        
        log.info("");
        log.info("📊 === PASO 5: ANÁLISIS FINAL ===");
        showFinalAnalysis(results, songsFromYaml, downloadService);
    }
    
    private void showBackendStatus(AdvancedDownloadService downloadService) {
        List<BackendStatus> statuses = downloadService.getBackendStatus();
        
        log.info("🔧 Estado de backends (orden de prioridad para fallback):");
        for (int i = 0; i < statuses.size(); i++) {
            BackendStatus status = statuses.get(i);
            String priority = switch (i) {
                case 0 -> "🥇 Primario (más rápido)";
                case 1 -> "🥈 Secundario (más robusto)";
                case 2 -> "🥉 Terciario (fallback final)";
                default -> "📊 Adicional";
            };
            
            log.info("   {} - {}", priority, status);
        }
    }
    
    private void showDownloadResults(List<DownloadResult> results, List<String> originalSongs) {
        log.info("📊 Resultados de descarga con fallback automático:");
        
        int successful = 0;
        int failed = 0;
        
        for (int i = 0; i < results.size(); i++) {
            DownloadResult result = results.get(i);
            String songName = i < originalSongs.size() ? originalSongs.get(i) : "Unknown";
            
            if (result.isSuccess()) {
                successful++;
                log.info("   ✅ {}: {} ({}) - Backend: {}", 
                        songName, 
                        result.getFileName(), 
                        result.getFormattedFileSize(),
                        result.getBackendUsed());
            } else {
                failed++;
                log.info("   ❌ {}: {}", songName, result.getErrorMessage());
            }
        }
        
        log.info("📈 Resumen: {}/{} exitosas ({:.1f}% éxito)", 
                successful, results.size(), 
                results.size() > 0 ? (double) successful / results.size() * 100 : 0);
    }
    
    private void showFinalAnalysis(List<DownloadResult> results, List<String> songsFromYaml, 
                                 AdvancedDownloadService downloadService) {
        
        long successfulCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        double successRate = results.size() > 0 ? (double) successfulCount / results.size() * 100 : 0;
        
        log.info("🎯 Análisis del sistema YAML + Fallback:");
        log.info("   • Canciones desde YAML: {}", songsFromYaml.size());
        log.info("   • Intentos de descarga: {}", results.size());
        log.info("   • Descargas exitosas: {}", successfulCount);
        log.info("   • Tasa de éxito: {:.1f}%", successRate);
        
        log.info("");
        log.info("📊 Estado final de backends:");
        List<BackendStatus> finalStatuses = downloadService.getBackendStatus();
        finalStatuses.forEach(status -> log.info("   {}", status));
        
        log.info("");
        log.info("🔑 Ventajas del sistema YAML + Fallback:");
        log.info("   1. 📂 Configuración externa: Canciones en songs.yml");
        log.info("   2. 🔄 Fallback automático: JTube → yt-dlp → HTTP Direct");
        log.info("   3. 🛡️ Resiliencia: Continúa aunque backends fallen");
        log.info("   4. 📊 Transparencia: Logs detallados de cada intento");
        log.info("   5. 🎵 Flexibilidad: Fácil modificar canciones sin recompilar");
        
        log.info("");
        log.info("💡 Para modificar las canciones:");
        log.info("   • Edita: src/test/resources/data/songs.yml");
        log.info("   • Formato: \"Título - Artista\"");
        log.info("   • El sistema cargará automáticamente los cambios");
        
        log.info("");
        log.info("=====================================");
    }
}
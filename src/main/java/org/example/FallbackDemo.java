package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.models.DownloadResult;
import org.example.services.AdvancedDownloadService;
import org.example.services.AdvancedDownloadService.BackendStatus;

import java.util.List;

/**
 * Demostración del sistema de fallback automático entre backends
 */
@Slf4j
public class FallbackDemo {
    
    public static void main(String[] args) {
        log.info("🚀 === DEMOSTRACIÓN DEL SISTEMA DE FALLBACK AUTOMÁTICO ===");
        
        FallbackDemo demo = new FallbackDemo();
        demo.demonstrateFallback();
    }
    
    public void demonstrateFallback() {
        log.info("🔧 Inicializando servicio de descarga avanzado...");
        
        // Crear servicio con fallback automático
        AdvancedDownloadService downloadService = new AdvancedDownloadService();
        
        // Mostrar estado inicial de backends
        showBackendStatus(downloadService);
        
        log.info("");
        log.info("🎵 === PRUEBA DE FALLBACK CON CANCIONES REALES ===");
        
        // Lista de canciones para probar
        List<String> testSongs = List.of(
                "Cuando zarpa el amor - Camela",
                "Despacito - Luis Fonsi", 
                "Shape of You - Ed Sheeran"
        );
        
        // Probar descarga múltiple con fallback
        log.info("🔄 Iniciando descarga múltiple con fallback automático...");
        List<DownloadResult> results = downloadService.searchAndDownloadMultiple(
                testSongs, "./target/fallback-demo/");
        
        // Mostrar resultados detallados
        showDownloadResults(results, testSongs);
        
        log.info("");
        log.info("🧪 === PRUEBA DE FALLBACK CON CANCIÓN INEXISTENTE ===");
        
        // Probar con una canción que no existe para ver el comportamiento de fallback
        String nonExistentSong = "NonExistentSong12345XYZ";
        log.info("🔄 Probando fallback con canción inexistente: '{}'", nonExistentSong);
        
        DownloadResult result = downloadService.searchAndDownload(
                nonExistentSong, "./target/fallback-demo/");
        
        if (result.isSuccess()) {
            log.info("✅ Resultado inesperado: La canción inexistente se descargó");
        } else {
            log.info("❌ Comportamiento esperado: Todos los backends fallaron para la canción inexistente");
            log.info("   Error: {}", result.getErrorMessage());
        }
        
        log.info("");
        log.info("📊 === ESTADO FINAL DE BACKENDS ===");
        showBackendStatus(downloadService);
        
        log.info("");
        log.info("💡 === CONCLUSIONES DEL FALLBACK ===");
        showConclusions(results);
    }
    
    private void showBackendStatus(AdvancedDownloadService downloadService) {
        List<BackendStatus> statuses = downloadService.getBackendStatus();
        
        log.info("🔧 Estado de backends (en orden de prioridad):");
        for (int i = 0; i < statuses.size(); i++) {
            BackendStatus status = statuses.get(i);
            String priority = switch (i) {
                case 0 -> "🥇 Primario";
                case 1 -> "🥈 Secundario";
                case 2 -> "🥉 Terciario";
                default -> "📊 Adicional";
            };
            
            log.info("   {} - {}", priority, status);
        }
    }
    
    private void showDownloadResults(List<DownloadResult> results, List<String> originalSongs) {
        log.info("📊 Resultados de descarga con fallback:");
        
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
    
    private void showConclusions(List<DownloadResult> results) {
        long successfulCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        double successRate = results.size() > 0 ? (double) successfulCount / results.size() * 100 : 0;
        
        log.info("🎯 Efectividad del sistema de fallback:");
        log.info("   • Total de intentos: {}", results.size());
        log.info("   • Descargas exitosas: {}", successfulCount);
        log.info("   • Tasa de éxito: {:.1f}%", successRate);
        
        if (successRate > 0) {
            log.info("   ✅ El sistema de fallback funcionó correctamente");
            log.info("   📌 Al menos un backend pudo completar las descargas");
        } else {
            log.info("   ⚠️ Ningún backend pudo completar las descargas");
            log.info("   📌 Esto puede indicar problemas de red o configuración");
        }
        
        log.info("");
        log.info("🔑 Ventajas del sistema de fallback:");
        log.info("   1. 🛡️ Resiliencia: Si JTube falla, yt-dlp toma el control");
        log.info("   2. ⚡ Velocidad: JTube se intenta primero por ser más rápido");
        log.info("   3. 🔧 Flexibilidad: Fácil agregar nuevos backends");
        log.info("   4. 📊 Transparencia: Reporta qué backend fue exitoso");
        log.info("   5. 🔄 Automático: Sin intervención manual del usuario");
        
        log.info("=====================================");
    }
}
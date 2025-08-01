package org.example.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.example.models.DownloadResult;
import org.example.services.AdvancedDownloadService;
import org.example.services.AdvancedDownloadService.BackendStatus;
import org.example.utils.Actions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps de Cucumber para las pruebas de descarga con fallback automático
 * Utiliza las canciones definidas en songs.yml
 */
@Slf4j
public class DownloadSteps {

    private AdvancedDownloadService downloadService;
    private List<String> songsToDownload;
    private List<DownloadResult> downloadResults;
    private String outputDirectory;
    private final String DEFAULT_OUTPUT_PATH = "./target/";

    @Given("I have a list of songs to download")
    public void i_have_a_list_of_songs_to_download() {
        log.info("🎵 Cargando lista de canciones desde songs.yml");
        
        try {
            // Cargar canciones desde el archivo YAML usando la utilidad existente
            songsToDownload = Actions.getYmlFile("songs");
            
            log.info("📋 Lista cargada con {} canciones desde songs.yml:", songsToDownload.size());
            for (int i = 0; i < songsToDownload.size(); i++) {
                log.info("   {}. {}", i + 1, songsToDownload.get(i));
            }
            
            assertNotNull(songsToDownload, "La lista de canciones no debe ser nula");
            assertFalse(songsToDownload.isEmpty(), "La lista de canciones no debe estar vacía");
            
        } catch (Exception e) {
            log.error("❌ Error cargando canciones desde songs.yml: {}", e.getMessage());
            fail("Error cargando canciones desde songs.yml: " + e.getMessage());
        }
    }

    @Given("the advanced download service with fallback is available")
    public void the_advanced_download_service_with_fallback_is_available() {
        log.info("🚀 Inicializando servicio de descarga avanzado con fallback automático");
        
        downloadService = new AdvancedDownloadService();
        assertNotNull(downloadService, "El servicio de descarga no debe ser nulo");
        
        // Mostrar estado de backends disponibles
        List<BackendStatus> backendStatuses = downloadService.getBackendStatus();
        log.info("📊 Estado de backends:");
        backendStatuses.forEach(status -> log.info("   {}", status));
        
        // Verificar que al menos un backend esté disponible
        boolean hasAvailableBackend = backendStatuses.stream().anyMatch(status -> status.available);
        assertTrue(hasAvailableBackend, "Debe haber al menos un backend disponible");
        
        log.info("✅ Servicio de descarga avanzado listo");
    }

    @Given("the output directory is set to {string}")
    public void the_output_directory_is_set_to(String directory) {
        log.info("📁 Configurando directorio de salida: {}", directory);
        
        outputDirectory = directory;
        
        // Crear el directorio si no existe
        try {
            Path path = Paths.get(outputDirectory);
            Files.createDirectories(path);
            log.info("✅ Directorio de salida listo: {}", path.toAbsolutePath());
        } catch (Exception e) {
            log.error("❌ Error creando directorio: {}", e.getMessage());
            fail("No se pudo crear el directorio de salida: " + e.getMessage());
        }
    }

    @When("I download the songs with automatic fallback")
    public void i_download_the_songs_with_automatic_fallback() {
        log.info("🔄 Iniciando descarga con fallback automático para {} canciones desde songs.yml", 
                songsToDownload != null ? songsToDownload.size() : 0);
        
        assertNotNull(downloadService, "El servicio de descarga debe estar inicializado");
        assertNotNull(songsToDownload, "La lista de canciones no debe ser nula");
        assertFalse(songsToDownload.isEmpty(), "La lista de canciones no debe estar vacía");
        
        String targetPath = outputDirectory != null ? outputDirectory : DEFAULT_OUTPUT_PATH;
        
        try {
            downloadResults = downloadService.searchAndDownloadMultiple(songsToDownload, targetPath);
            
            log.info("📊 Descarga completada. Resultados:");
            for (int i = 0; i < downloadResults.size(); i++) {
                DownloadResult result = downloadResults.get(i);
                if (result.isSuccess()) {
                    log.info("   ✅ {}: {} ({})", 
                            songsToDownload.get(i), 
                            result.getFileName(),
                            result.getFormattedFileSize());
                } else {
                    log.info("   ❌ {}: {}", 
                            songsToDownload.get(i), 
                            result.getErrorMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error durante la descarga: {}", e.getMessage());
            fail("Error durante la descarga: " + e.getMessage());
        }
    }

    @When("I download a single song {string} with automatic fallback")
    public void i_download_a_single_song_with_automatic_fallback(String songName) {
        log.info("🎵 Descargando canción individual con fallback: '{}'", songName);
        
        assertNotNull(downloadService, "El servicio de descarga debe estar inicializado");
        
        String targetPath = outputDirectory != null ? outputDirectory : DEFAULT_OUTPUT_PATH;
        
        try {
            DownloadResult result = downloadService.searchAndDownload(songName, targetPath);
            
            downloadResults = List.of(result);
            
            if (result.isSuccess()) {
                log.info("✅ Descarga exitosa: {} ({})", 
                        result.getFileName(), result.getFormattedFileSize());
            } else {
                log.info("❌ Descarga fallida: {}", result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("❌ Error durante la descarga: {}", e.getMessage());
            fail("Error durante la descarga: " + e.getMessage());
        }
    }

    @When("I download songs from YAML with automatic fallback")
    public void i_download_songs_from_yaml_with_automatic_fallback() {
        log.info("🎵 Descargando canciones desde songs.yml con fallback automático");
        
        // Primero cargar las canciones si no están ya cargadas
        if (songsToDownload == null) {
            i_have_a_list_of_songs_to_download();
        }
        
        // Luego proceder con la descarga
        i_download_the_songs_with_automatic_fallback();
    }

    @Then("the download should be successful")
    public void the_download_should_be_successful() {
        log.info("🔍 Verificando que las descargas fueron exitosas");
        
        assertNotNull(downloadResults, "Los resultados de descarga no deben ser nulos");
        assertFalse(downloadResults.isEmpty(), "Debe haber al menos un resultado de descarga");
        
        long successfulDownloads = downloadResults.stream()
                .mapToLong(result -> result.isSuccess() ? 1 : 0)
                .sum();
        
        log.info("📊 Descargas exitosas: {}/{}", successfulDownloads, downloadResults.size());
        
        assertTrue(successfulDownloads > 0, 
                "Debe haber al menos una descarga exitosa con el sistema de fallback");
    }

    @Then("all downloads should be successful")
    public void all_downloads_should_be_successful() {
        log.info("🔍 Verificando que TODAS las descargas fueron exitosas");
        
        assertNotNull(downloadResults, "Los resultados de descarga no deben ser nulos");
        assertFalse(downloadResults.isEmpty(), "Debe haber al menos un resultado de descarga");
        
        for (int i = 0; i < downloadResults.size(); i++) {
            DownloadResult result = downloadResults.get(i);
            assertTrue(result.isSuccess(), 
                    String.format("La descarga %d (%s) debería ser exitosa. Error: %s", 
                            i + 1, 
                            songsToDownload != null && i < songsToDownload.size() ? songsToDownload.get(i) : "unknown",
                            result.getErrorMessage()));
        }
        
        log.info("✅ Todas las {} descargas fueron exitosas", downloadResults.size());
    }

    @Then("the downloaded files should exist in the output directory")
    public void the_downloaded_files_should_exist_in_the_output_directory() {
        log.info("🔍 Verificando que los archivos descargados existen");
        
        assertNotNull(downloadResults, "Los resultados de descarga no deben ser nulos");
        
        List<DownloadResult> successfulDownloads = downloadResults.stream()
                .filter(DownloadResult::isSuccess)
                .toList();
        
        assertTrue(successfulDownloads.size() > 0, 
                "Debe haber al menos una descarga exitosa para verificar archivos");
        
        for (DownloadResult result : successfulDownloads) {
            String filePath = result.getFilePath();
            assertNotNull(filePath, "La ruta del archivo no debe ser nula");
            
            File file = new File(filePath);
            assertTrue(file.exists(), 
                    String.format("El archivo debe existir: %s", filePath));
            assertTrue(file.length() > 0, 
                    String.format("El archivo no debe estar vacío: %s", filePath));
            
            log.info("✅ Archivo verificado: {} ({} bytes)", 
                    file.getName(), file.length());
        }
        
        log.info("✅ Todos los archivos descargados existen y no están vacíos");
    }

    @Then("the system should show backend fallback information")
    public void the_system_should_show_backend_fallback_information() {
        log.info("🔍 Verificando información de fallback de backends");
        
        assertNotNull(downloadService, "El servicio de descarga debe estar inicializado");
        
        List<BackendStatus> backendStatuses = downloadService.getBackendStatus();
        assertNotNull(backendStatuses, "El estado de backends no debe ser nulo");
        assertFalse(backendStatuses.isEmpty(), "Debe haber información de al menos un backend");
        
        log.info("📊 Estado final de backends:");
        backendStatuses.forEach(status -> {
            log.info("   {}", status);
            assertNotNull(status.name, "El nombre del backend no debe ser nulo");
            assertNotNull(status.status, "El estado del backend no debe ser nulo");
        });
        
        // Verificar que el sistema intentó con los backends en orden de prioridad
        boolean hasJTubeStatus = backendStatuses.stream()
                .anyMatch(status -> status.name.contains("Java nativa"));
        boolean hasYtDlpStatus = backendStatuses.stream()
                .anyMatch(status -> status.name.contains("yt-dlp"));
        
        assertTrue(hasJTubeStatus, "Debe mostrar estado de JTube (backend prioritario)");
        assertTrue(hasYtDlpStatus, "Debe mostrar estado de yt-dlp (backend secundario)");
        
        log.info("✅ Información de fallback verificada correctamente");
    }

    @Then("at least {int} downloads should be successful")
    public void at_least_downloads_should_be_successful(Integer minSuccessful) {
        log.info("🔍 Verificando que al menos {} descargas fueron exitosas", minSuccessful);
        
        assertNotNull(downloadResults, "Los resultados de descarga no deben ser nulos");
        
        long successfulDownloads = downloadResults.stream()
                .mapToLong(result -> result.isSuccess() ? 1 : 0)
                .sum();
        
        assertTrue(successfulDownloads >= minSuccessful, 
                String.format("Debe haber al menos %d descargas exitosas, pero solo hubo %d", 
                        minSuccessful, successfulDownloads));
        
        log.info("✅ Verificación completada: {}/{} descargas exitosas (mínimo requerido: {})", 
                successfulDownloads, downloadResults.size(), minSuccessful);
    }

    @Then("the songs should be loaded from songs.yml file")
    public void the_songs_should_be_loaded_from_songs_yml_file() {
        log.info("🔍 Verificando que las canciones se cargaron desde songs.yml");
        
        assertNotNull(songsToDownload, "La lista de canciones no debe ser nula");
        assertFalse(songsToDownload.isEmpty(), "La lista de canciones no debe estar vacía");
        
        // Verificar que las canciones contienen el formato "Canción - Artista"
        for (String song : songsToDownload) {
            assertNotNull(song, "Cada canción debe tener un valor");
            assertFalse(song.trim().isEmpty(), "Cada canción debe tener contenido");
            assertTrue(song.contains(" - "), 
                    String.format("La canción '%s' debe tener formato 'Título - Artista'", song));
        }
        
        log.info("✅ {} canciones verificadas desde songs.yml:", songsToDownload.size());
        songsToDownload.forEach(song -> log.info("   • {}", song));
    }
}

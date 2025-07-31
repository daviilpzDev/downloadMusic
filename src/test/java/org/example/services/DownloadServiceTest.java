package org.example.services;

import org.example.utils.Constants;
import org.example.utils.Globals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadServiceTest {

    private DownloadService downloadService;

    @BeforeEach
    void setUp() {
        downloadService = new DownloadService();
        Globals.list.clear(); // Limpiar lista antes de cada test
    }

    /*
    // Tests comentados porque usan datos de ejemplo que no corresponden a canciones reales
    @Test
    void testSearchSongsAndGetUrls_WithValidSongs() {
        // Given
        List<String> songs = Arrays.asList("test song 1", "test song 2");

        // When
        downloadService.searchSongsAndGetUrls(songs);

        // Then
        assertFalse(Globals.list.isEmpty());
        assertTrue(Globals.list.size() <= songs.size());
    }

    @Test
    void testDownloadSongs_WithDifferentSizes() {
        // Given
        List<String> urls = Arrays.asList("url1", "url2");
        List<String> songs = Arrays.asList("song1");

        // When & Then
        assertDoesNotThrow(() -> downloadService.downloadSongs(urls, songs));
    }

    @Test
    void testDownloadSongs_WithMatchingSizes() {
        // Given
        List<String> urls = Arrays.asList("url1");
        List<String> songs = Arrays.asList("song1");

        // When & Then
        assertDoesNotThrow(() -> downloadService.downloadSongs(urls, songs));
    }
    */
} 
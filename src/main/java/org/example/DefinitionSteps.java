package org.example;

import org.example.services.DownloadService;
import org.example.utils.Actions;
import org.example.utils.Globals;

import java.util.List;

public class DefinitionSteps {
    private static final DownloadService downloadService = new DownloadService();

    public static void searchSongAndGetTheUrl() {
        List<String> songs = Actions.getYmlFile("songs");
        downloadService.searchSongsAndGetUrls(songs);
    }

    public static void downloadSongAction() {
        List<String> urls = Globals.list;
        List<String> songs = Actions.getYmlFile("songs");
        downloadService.downloadSongs(urls, songs);
    }
}

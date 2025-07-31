package org.example;

import org.example.services.DownloadService;
import org.example.utils.Actions;
import org.example.utils.Constants;
import org.example.utils.Globals;
import org.slf4j.Logger;

import java.util.List;

public class Methods {
    private static final Logger logger = Constants.logger;
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

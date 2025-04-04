package org.example;

import org.example.hooks.Hooks;
import org.example.utils.Actions;
import org.example.utils.Constants;
import org.example.utils.Globals;
import org.slf4j.Logger;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Methods {

    private static final Logger logger = Constants.logger;

    public static void searchSongAndGetTheUrl() {
        List<String> songs = Actions.getYmlFile("songs");

        List<String> urls = Globals.list;

        for (String song : songs) {
            try {
                String[] command = new String[]{
                        "yt-dlp", "ytsearch:" + song, "--get-id"
                };

                System.out.println("Comando generado: " + String.join(" ", command));

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String videoUrl = reader.readLine();

                if (videoUrl != null && !videoUrl.isEmpty()) {
                    urls.add("https://www.youtube.com/watch?v=" + videoUrl);
                    logger.info("Found URL for song: " + song + " - " + videoUrl);
                } else {
                    logger.warn("No URL found for song: " + song);
                }

                process.waitFor();
            } catch (Exception e) {
                logger.error("Song searching error: " + song + ": " + e.getMessage());
            }
        }
    }

    public static void downloadSongAction() {
        List<String> songs = Globals.list;
        for (String song : songs) {
            try {
                String[] command = new String[]{
                        "yt-dlp", "-x", "--audio-format", "mp3", "-o", Hooks.downloadFilepath + "%(title)s.%(ext)s" , song
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line);
                }

                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = errorReader.readLine()) != null) {
                    logger.error(line);
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    logger.info("¡Descarga completada exitosamente!");
                } else {
                    logger.error("Hubo un error en la descarga.");
                }

            } catch (Exception e) {
                logger.error("Song downloading error: " + getSong(song) + ": " + e.getMessage());
            }
        }
    }

    private static String getSong(String url) {
        List<String> songs = Actions.getYmlFile("songs");
        List<String> urls = Globals.list;

        if (songs.size() != urls.size()) {
            return "Las listas de canciones y URLs tienen tamaños diferentes.";
        }

        Map<String, String> map = IntStream.range(0, songs.size())
                .boxed()
                .collect(Collectors.toMap(urls::get, songs::get));

        return map.getOrDefault(url, "Canción no encontrada");
    }

}

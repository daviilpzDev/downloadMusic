package org.example;

import org.example.hooks.Hooks;
import org.example.utils.Actions;
import org.example.utils.Constants;
import org.example.utils.Globals;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;
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
                String videoId = reader.readLine();

                if (videoId != null && !videoId.isEmpty()) {
                    String fullUrl = "https://www.youtube.com/watch?v=" + videoId;
                    urls.add(fullUrl);
                    logger.info("Found URL for song: " + song + " - " + fullUrl);
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
        List<String> urls = Globals.list;
        List<String> songs = Actions.getYmlFile("songs");

        if (urls.size() != songs.size()) {
            logger.error("Las listas de canciones y URLs tienen tamaños diferentes.");
            return;
        }

        Map<String, String> urlToSongMap = buildUrlToSongMap(urls, songs);

        for (String url : urls) {
            try {
                String songNameRaw = urlToSongMap.getOrDefault(url, "Unknown");
                File dir = new File(Hooks.downloadFilepath);
                File lastBefore = getLatestMp3File(dir);

                String[] command = new String[]{
                        "yt-dlp", "-x", "--audio-format", "mp3",
                        "-o", Hooks.downloadFilepath + "%(title)s.%(ext)s",
                        url
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

                    File lastAfter = getLatestMp3File(dir);

                    if (lastAfter != null && (lastBefore == null || !lastBefore.getName().equals(lastAfter.getName()))) {
                        String sanitizedTargetName = sanitizeFilename(songNameRaw) + ".mp3";
                        File newFile = new File(Hooks.downloadFilepath + sanitizedTargetName);

                        if (lastAfter.renameTo(newFile)) {
                            logger.info("Archivo renombrado a: " + newFile.getName());
                        } else {
                            logger.warn("No se pudo renombrar el archivo: " + lastAfter.getName());
                        }
                    } else {
                        logger.warn("No se detectó un nuevo archivo .mp3 descargado.");
                    }
                } else {
                    logger.error("Error durante la descarga de: " + songNameRaw);
                }

            } catch (Exception e) {
                logger.error("Song downloading error: " + url + ": " + e.getMessage());
            }
        }
    }

    private static Map<String, String> buildUrlToSongMap(List<String> urls, List<String> songs) {
        return IntStream.range(0, songs.size())
                .boxed()
                .collect(Collectors.toMap(urls::get, songs::get));
    }

    private static File getLatestMp3File(File dir) {
        File[] mp3Files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".mp3"));
        if (mp3Files == null || mp3Files.length == 0) return null;

        return Arrays.stream(mp3Files)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }

    private static String sanitizeFilename(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "").trim();
    }
}

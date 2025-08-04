package org.example.steps;

import io.cucumber.java.en.*;
import org.example.DefinitionSteps;
import org.example.utils.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DownloadSteps {
    private static final Logger logger = LoggerFactory.getLogger(DownloadSteps.class);

    @Given("I have a list of songs to download")
    public void i_have_a_list_of_songs_to_download() {
        // Load and validate the songs list from YAML configuration
        List<String> songs = Actions.getYmlFile("songs");
        logger.info("Loaded {} songs for download", songs.size());
        
        if (songs.isEmpty()) {
            throw new RuntimeException("No songs found in configuration file");
        }
        
        logger.info("Songs to download: {}", songs);
    }

    @When("I search for the songs and get their URLs")
    public void i_search_for_the_songs_and_get_their_urls() {
        DefinitionSteps.searchSongAndGetTheUrl();
    }

    @Then("I download the songs as MP3 files")
    public void i_download_the_songs_as_mp3_files() {
        DefinitionSteps.downloadSongAction();
    }
}

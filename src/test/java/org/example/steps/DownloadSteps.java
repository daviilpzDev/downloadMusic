package org.example.steps;

import io.cucumber.java.en.*;
import org.example.Methods;


import static org.example.hooks.Hooks.*;

public class DownloadSteps {

    @When("search song and get the url")
    public void searchSongAndGetTheUrl() {
        Methods.searchSongAndGetTheUrl();
    }

    @Then("I enter the video URL into the input field$")
    public void i_enter_the_video_url_into_the_input_field() {
        Methods.downloadSongAction();
    }
}

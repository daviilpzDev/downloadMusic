package org.example.steps;

import io.cucumber.java.en.*;
import org.example.utils.FileDownloadCompletedCondition;
import org.example.Methods;


import static org.example.hooks.Hooks.*;

public class DownloadSteps {

    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String url) {
        driver.get(url);
    }

    @When("search song and get the url")
    public void searchSongAndGetTheUrl() {
        Methods.searchSongAndGetTheUrl(wait);
    }

    @When("I enter the video URL into the input field$")
    public void i_enter_the_video_url_into_the_input_field() {
        Methods.downloadSongAction(wait);
    }

    @When("login to jdownloader")
    public void loginToJdownloader() {
        Methods.jdownloaderLogin(wait);
    }

    @Then("deletes not needed files")
    public void deletesNotNeededFiles() {
        Methods.deleteNotNeededFiles();
    }
}

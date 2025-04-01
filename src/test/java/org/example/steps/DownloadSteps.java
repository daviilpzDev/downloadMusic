package org.example.steps;

import io.cucumber.java.en.*;
import org.example.FileDownloadCompletedCondition;
import org.example.Locators;
import org.example.Methods;


import static org.example.hooks.Hooks.*;

public class DownloadSteps {

    @Given("I navigate to the {string} converter page")
    public void i_navigate_to_the_converter_page(String url) {
        driver.get(url);
    }

    @When("I enter the video URL (.*) into the input field$")
    public void i_enter_the_video_url_into_the_input_field(String videoUrl) {
        Methods.clickAndSend(wait, Locators.VIDEO_INPUT, videoUrl);
    }

    @When("I click on the submit button")
    public void i_click_on_the_submit_button() {
        Methods.clickElement(wait, Locators.SUBMIT_BUTTON);
    }

    @When("I click on the download button")
    public void i_click_on_the_download_button() {
        Methods.clickElement(wait, Locators.DOWNLOAD_BUTTON);
    }

    @Then("the file should be downloaded successfully")
    public void the_file_should_be_downloaded_successfully() {
        wait.until(new FileDownloadCompletedCondition(downloadFilepath));
        Methods.moveFileToSMB(downloadFilepath);
    }
}

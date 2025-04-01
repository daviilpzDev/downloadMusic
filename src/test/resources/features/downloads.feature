Feature: Convert and download YouTube video to MP3

    As a user
    I want to convert a YouTube video to MP3 and download it
    So that I can listen to music offline

    Scenario Outline: Download music successfully
        Given I navigate to the "https://ytmp3.la/rTmi/" converter page
        When I enter the video URL <urlMusic> into the input field
        * I click on the submit button
        * I click on the download button
        Then the file should be downloaded successfully

        Examples:
        | urlMusic |
        | https://www.youtube.com/watch?v=evMXwRmMWbg|


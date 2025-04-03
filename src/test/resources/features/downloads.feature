Feature: Convert and download YouTube video to MP3

    As a user
    I want to convert a YouTube video to MP3 and download it
    So that I can listen to music offline

    Scenario: Download music successfully
        Given search song and get the url
        * I navigate to "http://my.jdownloader.org" page
        When login to jdownloader
        * I enter the video URL into the input field
        Then deletes not needed files



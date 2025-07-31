Feature: Convert and download YouTube video to MP3

    As a user
    I want to convert a YouTube video to MP3 and download it
    So that I can listen to music offline

    Scenario: Download music successfully
        Given I have a list of songs to download
        When I search for the songs and get their URLs
        Then I download the songs as MP3 files



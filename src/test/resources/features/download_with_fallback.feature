Feature: Download music with automatic backend fallback
  As a user
  I want to download music using multiple backends with automatic fallback
  So that downloads succeed even if the primary backend fails

  Background:
    Given the advanced download service with fallback is available
    And the output directory is set to "./target/"

  Scenario: Download songs from YAML file with automatic fallback
    Given I have a list of songs to download
    Then the songs should be loaded from songs.yml file
    When I download the songs with automatic fallback
    Then the system should show backend fallback information

  Scenario: Download songs directly from YAML with fallback
    When I download songs from YAML with automatic fallback
    Then the system should show backend fallback information

  Scenario: Download single song from YAML list with automatic fallback
    Given I have a list of songs to download
    When I download a single song "Cuando zarpa el amor - Camela" with automatic fallback
    Then the system should show backend fallback information

  Scenario: Verify YAML song loading and fallback behavior
    Given I have a list of songs to download
    Then the songs should be loaded from songs.yml file
    When I download the songs with automatic fallback
    Then the system should show backend fallback information

  Scenario: System gracefully handles multiple backend failures
    Given the advanced download service with fallback is available
    When I download a single song "NonExistentSong12345XYZ" with automatic fallback
    Then the system should show backend fallback information
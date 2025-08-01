Feature: Download music with automatic backend fallback
  As a user
  I want to download music using multiple backends with automatic fallback
  So that downloads succeed even if the primary backend fails

  Background:
    Given the advanced download service with fallback is available
    And the output directory is set to "./target/"

  Scenario: Download multiple songs with automatic fallback
    Given I have a list of songs to download
    When I download the songs with automatic fallback
    Then the download should be successful
    And the downloaded files should exist in the output directory
    And the system should show backend fallback information

  Scenario: Download single song with automatic fallback
    Given the advanced download service with fallback is available
    When I download a single song "Cuando zarpa el amor - Camela" with automatic fallback
    Then the download should be successful
    And the downloaded files should exist in the output directory

  Scenario: Fallback system ensures high success rate
    Given I have a list of songs to download
    When I download the songs with automatic fallback
    Then at least 1 downloads should be successful
    And the system should show backend fallback information

  Scenario: System gracefully handles multiple backend failures
    Given the advanced download service with fallback is available
    When I download a single song "NonExistentSong12345XYZ" with automatic fallback
    Then the system should show backend fallback information
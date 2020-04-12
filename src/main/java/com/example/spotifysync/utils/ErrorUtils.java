package com.example.spotifysync.utils;

import org.springframework.ui.Model;

/**
 * Class that helps set Error messages for the application
 */
public class ErrorUtils {
  public static void addStandardSpotifyAuthErrorToModel(final Model model) {
    model.addAttribute("error", "Encountered error while authenticating to Spotify, please try again");
  }
}

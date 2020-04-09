package com.example.spotifysync;

import com.google.gson.Gson;

import com.example.spotifysync.schema.SpotifyCurrentPlaying;
import com.example.spotifysync.utils.SpotifyUtils;
import com.example.spotifysync.utils.YouTubeUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * This file runs the Spring Application and is also a Spring MVC Controler for the Spotify Youtube
 * Application
 */
@Controller
@SpringBootApplication
public class Application {
  private final SpotifyUtils spotifyUtils = new SpotifyUtils();
  private final YouTubeUtils youTubeUtils = new YouTubeUtils();

  /**
   * Routes request to the index page. Currently a place holder.
   */
  @GetMapping("/")
  public String home() {
    return "index";
  }

  /**
   * Routes request to the index page. Currently a place holder.
   */
  @GetMapping("/index")
  public String index() {
    return "index";
  }

  /**
   * Error routing
   */
  @GetMapping("/error")
  public String error() {
    return "error";
  }

  /**
   * Endpoint that calls Spotify to get playback information for the authenticated user
   */
  @GetMapping(value = "/sync")
  @ResponseBody
  public Model sync(
      @CookieValue(value = "access_token", defaultValue = "") String accessToken,
      final @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken,
      final Model model,
      final HttpServletResponse httpServletResponse
  ) {

    //Check Access Token
    accessToken = spotifyUtils.verifyOrRefreshSpotifyAccessToken(accessToken, refreshToken, httpServletResponse, model);
    if (accessToken == null) {
      //error
      return model.addAttribute("error", "Could not get access token from Spotify");
    }

    //Ask Spotify for Current Playing
    final SpotifyCurrentPlaying currentPlaying = spotifyUtils.getCurrentPlayingFromSpotify(accessToken);

    if (currentPlaying == null) {
      return model.addAttribute("error", "Error while retrieving most recent track from Spotify");
    } else if (currentPlaying.isEmpty()) {
      return model.addAttribute("empty", "No data found");
    }

    //Check YouTube
    final String youTubeLink = youTubeUtils.getYouTubeLinkFromSpotifyTrack(currentPlaying);
    model.addAttribute("youTube", youTubeLink);
    model.addAttribute("progress", currentPlaying.getProgressMs() / 1000 + 1);
    model.addAttribute("isPlaying", currentPlaying.isPlaying());

    //Return results
    return model;
  }

  /**
   * Endpoint that calls Spotify to get updated playback information for the authenticated user
   */
  @GetMapping(value = "/update",  produces="application/json")
  @ResponseBody
  public Model update(
      @CookieValue(value = "access_token", defaultValue = "") String accessToken,
      final @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken,
      final Model model,
      final HttpServletResponse httpServletResponse
  ) {

    //Check Access Token
    accessToken = spotifyUtils.verifyOrRefreshSpotifyAccessToken(accessToken, refreshToken, httpServletResponse, model);
    if (accessToken == null) {
      //error
      return model.addAttribute("error", "Could not get access token from Spotify");
    }

    //Ask Spotify for Current Playing
    final SpotifyCurrentPlaying currentPlaying = spotifyUtils.getCurrentPlayingFromSpotify(accessToken);

    if (currentPlaying == null) {
      return model.addAttribute("error", "Error while retrieving most recent track from Spotify");
    } else if (currentPlaying.isEmpty()) {
      return model.addAttribute("empty", "No data found");
    }

    //Check YouTube
    final String youTubeLink = youTubeUtils.getYouTubeLinkFromSpotifyTrack(currentPlaying);
    model.addAttribute("youTube", youTubeLink);
    model.addAttribute("progress", currentPlaying.getProgressMs() / 1000 + 1);
    model.addAttribute("isPlaying", currentPlaying.isPlaying());

    //Return results
    return model;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

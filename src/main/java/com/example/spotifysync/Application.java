package com.example.spotifysync;

import com.example.spotifysync.schema.FrontendPlayResponse;
import com.example.spotifysync.schema.SpotifyCurrentPlaying;
import com.example.spotifysync.utils.SpotifyUtils;
import com.example.spotifysync.utils.YouTubeUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
   * Endpoint that calls Spotify to get updated playback information for the authenticated user
   */
  @GetMapping(value = "/update",  produces="application/json")
  @ResponseBody
  public FrontendPlayResponse update(
      @CookieValue(value = "access_token", defaultValue = "") String accessToken,
      final @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken,
      final Model model,
      final HttpServletResponse httpServletResponse
  ) {
    Map<String, String> response = new HashMap<>();
    //Check Access Token
    accessToken = spotifyUtils.verifyOrRefreshSpotifyAccessToken(accessToken, refreshToken, httpServletResponse, model);
    if (accessToken == null) {
      //error
      return new FrontendPlayResponse("Could not get access token from Spotify");
    }
    System.out.println("Found access token");

    //Ask Spotify for Current Playing
    final SpotifyCurrentPlaying currentPlaying = spotifyUtils.getCurrentPlayingFromSpotify(accessToken);
    System.out.println("Got current playing: " + currentPlaying);

    if (currentPlaying == null) {
      return new FrontendPlayResponse("Could not get access token from Spotify");
    } else if (currentPlaying.isEmpty()) {
      response.put("empty", "No data found");
      return new FrontendPlayResponse();
    }

    //Check YouTube
    final String youTubeId = youTubeUtils.getYouTubeLinkFromSpotifyTrack(currentPlaying);
    System.out.println("YT ID: " + youTubeId);
    response.put("youTube", youTubeId);
    response.put("progress", String.valueOf(currentPlaying.getProgressSeconds() + 1));
    response.put("isPlaying", String.valueOf(currentPlaying.isPlaying()));

    final FrontendPlayResponse frontendPlayResponse =
        new FrontendPlayResponse(
            currentPlaying.getSpotifyUri(),
            youTubeId,
            currentPlaying.getProgressSeconds() + 1,
            currentPlaying.isPlaying());
    //Return results
    return frontendPlayResponse;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

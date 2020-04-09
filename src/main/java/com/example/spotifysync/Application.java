package com.example.spotifysync;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.example.spotifysync.schema.SpotifyCurrentPlaying;
import com.example.spotifysync.utils.SpotifyUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This file runs the Spring Application and is also a Spring MVC Controler for the Spotify Youtube
 * Application
 */
@Controller
@SpringBootApplication
public class Application {
  private final OkHttpClient httpClient = new OkHttpClient();
  private final SpotifyUtils spotifyUtils = new SpotifyUtils();
  private final Gson gson = new Gson();

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
  @RequestMapping(value = "/sync", method = RequestMethod.GET)
  public ModelAndView sync(
      @CookieValue(value = "access_token", defaultValue = "") String accessToken,
      final @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken,
      final Model model,
      final HttpServletResponse httpServletResponse
  ) {

    //Check Access Token
    accessToken = spotifyUtils.verifyOrRefreshSpotifyAccessToken(accessToken, refreshToken, httpServletResponse, model);
    if (accessToken == null) {
      //error
      return new ModelAndView("redirect:/error", "error", "Error while getting authorization from Spotify");
    }

    //Ask Spotify for Current Playing
    final SpotifyCurrentPlaying currentPlaying = getCurrentPlayingFromSpotify(accessToken);

    if (currentPlaying == null) {
      return new ModelAndView("redirect:/error", "error", "Error while retrieving Current Track from Spotify");
    } else if (currentPlaying.isEmpty()) {
      return new ModelAndView("sync", "data", "No tracks playing");
    }

    //Check YouTube
    final String youTubeLink = getYouTubeLinkFromSpotifyTrack(currentPlaying);
    Map<String, Object> models = new HashMap<String, Object>();
    models.put("youTube", youTubeLink);
    models.put("progress", currentPlaying.getProgressMs() / 1000 + 1);
    models.put("isPlaying", currentPlaying.isPlaying());
    return new ModelAndView("sync", models);
    //Return results
  }

  private SpotifyCurrentPlaying getCurrentPlayingFromSpotify(final String accessToken) {
    final Request spotifyCurrentPlayingRequest = new Request.Builder()
        .url("https://api.spotify.com/v1/me/player/currently-playing")
        .addHeader("Authorization", "Bearer " + accessToken)
        .build();
    try {
      Response spotifyCurrentPlayingResponse = httpClient.newCall(spotifyCurrentPlayingRequest)
          .execute();
      if (!spotifyCurrentPlayingResponse.isSuccessful()) {
        System.out.println("Error while trying to fetch currently playing track from Spotify. Response not successful. Message: " + spotifyCurrentPlayingResponse
            .body());
        return null;
      } else {
        final String responseBody = spotifyCurrentPlayingResponse.body().string();
        System.out.println(responseBody);
        // Get response body
        JsonObject responseJson = new Gson().fromJson(responseBody, JsonObject.class);

        if (responseBody.isEmpty()) {
          return new SpotifyCurrentPlaying();
        }
        try {
          final JsonObject track_object = responseJson.get("item").getAsJsonObject();
          if (track_object != null) {

            final boolean isPlaying = responseJson.get("is_playing").getAsBoolean();
            final int durationMs = track_object.get("duration_ms").getAsInt();
            final int progressMs = responseJson.get("progress_ms").getAsInt();
            final String trackName = track_object.get("name").getAsString();

            final List<String> artists = new ArrayList<>();
            if (track_object.has("artists")) {
              final JsonArray artistArray = track_object.getAsJsonArray("artists");
              for (int i = 0; i < artistArray.size(); i++) {
                final String artistName = artistArray.get(i)
                    .getAsJsonObject()
                    .get("name")
                    .getAsString();
                artists.add(artistName);
              }
            }

            return new SpotifyCurrentPlaying(durationMs, progressMs, trackName, artists, isPlaying);
          } else {
            return new SpotifyCurrentPlaying();
          }
        } catch (Exception e) {
          System.out.println("Encountered error while parsing Spotify response. error: " + e.getMessage());
        }
      }
    } catch (IOException | NullPointerException e) {
      System.out.println("Encountered error while fetching currently playing track from Spotify. Error: " + e
          .getMessage());
      System.out.println(Arrays.toString(e.getStackTrace()));
    }
    return null;
  }

  private String getYouTubeLinkFromSpotifyTrack(SpotifyCurrentPlaying currentPlaying) {
    final String youtubeApiUrl =
        String.format("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&q=%s&key=%s",
            currentPlaying.getYouTubeSearchParams(), getYouTubeApiKey());

    final Request youTubeGetApplicableVideosRequest = new Request.Builder()
        .url(youtubeApiUrl)
        .build();

    try {
      Response youTubeApplicableVideosResponse = httpClient.newCall(youTubeGetApplicableVideosRequest)
          .execute();
      if (!youTubeApplicableVideosResponse.isSuccessful()) {
        System.out.println("Error while trying to get videos from YouTube. Response not successful. Message: " + youTubeApplicableVideosResponse
            .body());
        return null;
      } else {
        final String responseBody = youTubeApplicableVideosResponse.body().string();
        JsonObject responseJson = new Gson().fromJson(responseBody, JsonObject.class);

        return getBestYouTubeVideoFromResponse(currentPlaying, responseJson);
      }
    } catch (IOException ignored) {
    }
    return "";
  }

  private String getBestYouTubeVideoFromResponse(final SpotifyCurrentPlaying currentPlaying,
      final JsonObject youTubeResponse) {
    String bestYouTubeLink = "";
    int bestFit = 0;

    if (youTubeResponse.has("items")) {
      final JsonArray videoResults = youTubeResponse.getAsJsonArray("items");
      for (int i = 0; i < videoResults.size(); i++) {
        final JsonObject videoResult = videoResults.get(i)
            .getAsJsonObject();

        final JsonObject snippet = videoResult
            .get("snippet")
            .getAsJsonObject();

        final String videoTitle = snippet.get("title").getAsString();
        final String channelTitle = snippet.get("channelTitle").getAsString();

        int similarity = currentPlaying.compareSpotifyTrackToYouTubeVideo(videoTitle, channelTitle);

        System.out.println("Result: " + i + ", " + videoResult.get("id")
            .getAsJsonObject()
            .get("videoId")
            .getAsString());

        if (similarity > bestFit) {
          bestFit = similarity;
          bestYouTubeLink = videoResult.get("id").getAsJsonObject().get("videoId").getAsString();
          System.out.println(similarity + " " + bestFit + " " + bestYouTubeLink);
        }
      }
    }
    return bestYouTubeLink;
  }

  private String getYouTubeApiKey() {
    return System.getenv("YOUTUBE_API_KEY");
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

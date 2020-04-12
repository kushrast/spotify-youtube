package com.example.spotifysync.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.example.spotifysync.schema.SpotifyCurrentPlaying;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Utils class to interface with YouTube
 */
public class YouTubeUtils {
  public YouTubeUtils() {
  }

  private final OkHttpClient httpClient = new OkHttpClient();
  private final StringSimilarityService stringSimilarityService = new StringSimilarityServiceImpl(new JaroWinklerStrategy());

  public String getYouTubeLinkFromSpotifyTrack(SpotifyCurrentPlaying currentPlaying) {
    final String youtubeApiUrl =
        String.format("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&q=%s&key=%s",
            currentPlaying.getYouTubeSearchParams(), getYouTubeApiKey());

    final Request youTubeGetApplicableVideosRequest = new Request.Builder()
        .url(youtubeApiUrl)
        .build();

    try {
      Response youTubeApplicableVideosResponse = httpClient.newCall(youTubeGetApplicableVideosRequest)
          .execute();
      final String youTubeResponse = youTubeApplicableVideosResponse
          .body().string();
      if (!youTubeApplicableVideosResponse.isSuccessful()) {
        System.out.println("Error while trying to get videos from YouTube. Response not successful. Message: " + youTubeResponse);
        return null;
      } else {
        JsonObject responseJson = new Gson().fromJson(youTubeResponse, JsonObject.class);

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

        int similarity = currentPlaying.compareSpotifyTrackToYouTubeVideo(stringSimilarityService, videoTitle, channelTitle);

        if (similarity > bestFit) {
          bestFit = similarity;
          bestYouTubeLink = videoResult.get("id").getAsJsonObject().get("videoId").getAsString();
        }
      }
    }
    return bestYouTubeLink;
  }

  private String getYouTubeApiKey() {
    return System.getenv("YOUTUBE_API_KEY");
  }
}

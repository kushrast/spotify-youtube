package com.example.spotifysync.utils;

import static com.example.spotifysync.utils.CookieUtils.setCookie;
import static com.example.spotifysync.utils.ErrorUtils.addStandardSpotifyAuthErrorToModel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.example.spotifysync.schema.SpotifyCurrentPlaying;

import org.springframework.ui.Model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Utils file that calls Spotify and provides Spotify client secrets
 *
 * //TODO: Add a Spotify Client?
 */
public class SpotifyUtils {
  public SpotifyUtils() {
  }

  //TODO: Use Dagger
  private final OkHttpClient httpClient = new OkHttpClient();

  public static final String SPOTIFY_AUTH_CALLBACK = "/spotify_auth_callback";

  /**
   * Verifies that the provided access token is valid. If not, refreshes access token from Spotify.
   */
  public String verifyOrRefreshSpotifyAccessToken(String accessToken, String refreshToken,
      HttpServletResponse httpServletResponse, Model model) {
    if (refreshToken.equals("")) {
      //TODO: Tell user their authentication has expired
      return null;
    } else if (accessToken.equals("")) {
      System.out.println("Empty Access Token");
      return refreshAccessToken(refreshToken, model, httpServletResponse);
    }
    return accessToken;
  }


  /**
   * Makes call to Spotify to fetch access token using refresh_token. Set cookie for access token
   */
  private String refreshAccessToken(final String refresh_token, final Model model,
      final HttpServletResponse httpServletResponse) {

    final RequestBody formBody =
        new FormBody.Builder()
            .add("refresh_token", refresh_token)
            .add("grant_type", "refresh_token")
            .build();

    Request request = new Request.Builder()
        .url("https://accounts.spotify.com/api/token")
        .addHeader("Authorization", getSpotifyAuthHeader())
        .post(formBody)
        .build();


    try {
      Response spotifyAuthResponse = httpClient.newCall(request).execute();
      if (!spotifyAuthResponse.isSuccessful()) {
        System.out.println("Error while trying to fetch authentication token from Spotify. Response not successful.");
        addStandardSpotifyAuthErrorToModel(model);
      }

      // Get response body
      JsonObject responseJson = new Gson().fromJson(
          spotifyAuthResponse.body().string(), JsonObject.class);

      final int expiresIn = responseJson.get("expires_in").getAsInt();

      // Set cookies with values
      setCookie("access_token", responseJson.get("access_token")
          .getAsString(), expiresIn, httpServletResponse);
      return responseJson.get("access_token").getAsString();
    } catch (IOException | NullPointerException e) {
      System.out.println("Encountered error while fetching access_token from Spotify. Error: " + e
          .getMessage());
      System.out.println(Arrays.toString(e.getStackTrace()));
      addStandardSpotifyAuthErrorToModel(model);
    }
    return null;
  }

  /**
   * Makes call to Spotify to fetch access token using auth code. Sets cookies with access and
   * refresh tokens.
   *
   * Returns true if executed without errors.
   */
  public boolean fetchAccessTokenFromAuthCode(final String code,
      final Model model,
      final HttpServletResponse httpServletResponse) {

    final RequestBody formBody =
        new FormBody.Builder()
            .add("code", code)
            .add("redirect_uri", getSpotifyRedirectUrl())
            .add("grant_type", "authorization_code")
            .build();

    Request request = new Request.Builder()
        .url("https://accounts.spotify.com/api/token")
        .addHeader("Authorization", getSpotifyAuthHeader())
        .post(formBody)
        .build();


    try {
      Response spotifyAuthResponse = httpClient.newCall(request).execute();
      if (!spotifyAuthResponse.isSuccessful()) {
        System.out.println("Error while trying to fetch authentication token from Spotify. Response not successful.");
        addStandardSpotifyAuthErrorToModel(model);
      }

      // Get response body
      JsonObject responseJson = new Gson().fromJson(
          spotifyAuthResponse.body().string(), JsonObject.class);

      final int expiresIn = responseJson.get("expires_in").getAsInt();

      // Set cookies with values
      setCookie("access_token", responseJson.get("access_token")
          .getAsString(), expiresIn, httpServletResponse);
      setCookie("refresh_token", responseJson.get("refresh_token")
          .getAsString(), httpServletResponse);
      return true;

    } catch (IOException | NullPointerException e) {
      System.out.println("Encountered error while fetching access_token from Spotify. Error: " + e
          .getMessage());
      System.out.println(Arrays.toString(e.getStackTrace()));
      addStandardSpotifyAuthErrorToModel(model);
    }
    return false;
  }

  public SpotifyCurrentPlaying getCurrentPlayingFromSpotify(final String accessToken) {
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
            final String trackUri = track_object.get("uri").getAsString();

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

            return new SpotifyCurrentPlaying(durationMs, progressMs, trackName, artists, isPlaying, trackUri);
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

  /**
   * Assembles Spotify Authorization URL to send users to in order to get credentials
   */
  public static String buildSpotifyAuthorizationLink(final String state) {
    // Request authorization for Spotify by redirecting user to spotify
    final String responseType = "code";
    final String scope = "user-read-currently-playing";
    final String clientId = getSpotifyClientId();
    final String redirectUri = getSpotifyRedirectUrl();
    return String.format(
        "https://accounts.spotify.com/authorize?response_type=%s&client_id=%s&scope=%s&redirect_uri=%s&state=%s",
        responseType,
        clientId,
        scope,
        redirectUri,
        state
    );
  }

  private static String getSpotifyClientId() {
    return System.getenv("SPOTIFY_CLIENT_ID");
  }

  private static String getSpotifyClientSecret() {
    return System.getenv("SPOTIFY_CLIENT_SECRET");
  }

  private static String getSpotifyAuthHeader() {
    String clientCreds = String.format("%s:%s", getSpotifyClientId(), getSpotifyClientSecret());
    String encodedCreds = Base64.getEncoder().encodeToString(clientCreds.getBytes());

    return "Basic " + encodedCreds;
  }

  private static String getSpotifyRedirectUrl() {
    return getServerUrl() + SPOTIFY_AUTH_CALLBACK;
  }

  private static String getServerUrl() {
    return "https://sync-kr.herokuapp.com";
  }
}

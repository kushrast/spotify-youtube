package com.example.spotifysync;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This file runs the Spring Application and is also a Spring MVC Controler for the Spotify Youtube
 * Application
 */
@Controller
@SpringBootApplication
public class Application {
  private static final String SPOTIFY_AUTH_CALLBACK = "/spotify_auth_callback";

  private final OkHttpClient httpClient = new OkHttpClient();
  private final Gson gson = new Gson();

  /**
   * Routes request to the index page. Currently a place holder.
   */
  @GetMapping("/")
  public String home(
      @RequestParam(name = "name", required = false, defaultValue = "World") String name,
      Model model) {
    model.addAttribute("name", name);
    return "index";
  }

  /**
   * Redirects a user to Spotify's web authentication page in order to get API access tokens
   */
  @RequestMapping(value = "/authenticate_spotify", method = RequestMethod.GET)
  public void authenticateWithSpotify(HttpServletResponse httpServletResponse) {
    // Generate a random state string and save in a cookie for verification
    final String state = UUID.randomUUID().toString();
    final Cookie stateCookie = new Cookie("state", state);
    stateCookie.setPath("/");
    stateCookie.setSecure(true);
    stateCookie.setHttpOnly(true);

    // Request authorization for Spotify by redirecting user to spotify
    final String responseType = "code";
    final String scope = "user-read-currently-playing";
    final String clientId = getSpotifyClientId();
    final String redirectUri = getSpotifyRedirectUrl();
    final String spotifyAuthLink =
        String.format(
            "https://accounts.spotify.com/authorize?response_type=%s&client_id=%s&scope=%s&redirect_uri=%s&state=%s",
            responseType,
            clientId,
            scope,
            redirectUri,
            state
        );

    httpServletResponse.addCookie(stateCookie);
    httpServletResponse.setHeader("Location", spotifyAuthLink);
    httpServletResponse.setStatus(302);
  }

  /**
   * Endpoint called by Spotify after user completes authorization. Recieves an auth code from
   * Spotify that can be exchanged for an access token and refresh token. Makes call to Spotify to
   * fetch access token using auth code.
   *
   * TODO: Set cookie with access token/refresh token
   */
  @RequestMapping(value = SPOTIFY_AUTH_CALLBACK, method = RequestMethod.GET)
  public String spotifyAuthCallback(
      final @CookieValue(value = "state", defaultValue = "") String storedState,
      final @RequestParam(name = "code", required = false, defaultValue = "") String code,
      final @RequestParam(name = "state", required = true, defaultValue = "") String state,
      final @RequestParam(name = "error", required = false, defaultValue = "") String error,
      final Model model,
      final HttpServletResponse httpServletResponse
  ) {

    //Removing state cookie
    final Cookie stateCookie = new Cookie("state", null);
    stateCookie.setMaxAge(0);
    stateCookie.setSecure(true);
    stateCookie.setHttpOnly(true);
    stateCookie.setPath("/");
    //add cookie to response
    httpServletResponse.addCookie(stateCookie);

    if (!storedState.equals(state)) {
      System.out.println("Stored state does not match state returned from Spotify");
      System.out.println("Authorization failed. States do not match.");
    } else if (!code.equals("")) {

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
          System.out.println("Error while trying to fetch authentication token from Spotify");
          model.addAttribute("name", "error");
        }

        // Get response body
        JsonObject responseJson = new Gson().fromJson(
            spotifyAuthResponse.body().string(), JsonObject.class);

        System.out.println("Access Token: " + responseJson.get("access_token"));
        System.out.println("Refresh Token: " + responseJson.get("refresh_token"));
        model.addAttribute("name", "token: " + code);
      } catch (IOException | NullPointerException ignored) {
      }

    } else {
      System.out.println("Authorization failed. Error messge: " + error);
      model.addAttribute("name", "failure: " + error);
    }
    return "index";
  }

  private String getSpotifyClientId() {
    return System.getenv("SPOTIFY_CLIENT_ID");
  }

  private String getSpotifyClientSecret() {
    return System.getenv("SPOTIFY_CLIENT_SECRET");
  }

  private String getSpotifyAuthHeader() {
    String clientCreds = String.format("%s:%s", getSpotifyClientId(), getSpotifyClientSecret());
    String encodedCreds = Base64.getEncoder().encodeToString(clientCreds.getBytes());

    return "Basic " + encodedCreds;
  }

  private String getSpotifyRedirectUrl() {
    return getServerUrl() + SPOTIFY_AUTH_CALLBACK;
  }

  private String getServerUrl() {
    return "https://spotify-youtube.herokuapp.com";
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

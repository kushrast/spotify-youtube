package com.example.spotifysync;

import static com.example.spotifysync.utils.CookieUtils.clearCookie;
import static com.example.spotifysync.utils.CookieUtils.setServerCookie;
import static com.example.spotifysync.utils.ErrorUtils.addStandardSpotifyAuthErrorToModel;
import static com.example.spotifysync.utils.SpotifyUtils.SPOTIFY_AUTH_CALLBACK;
import static com.example.spotifysync.utils.SpotifyUtils.buildSpotifyAuthorizationLink;

import com.example.spotifysync.utils.SpotifyUtils;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

/**
 * Controller class that provides mappings for endpoints related to Spotify Authentication
 */
@Controller
public class SpotifyAuthController {
  //TODO: Use Dagger
  private static SpotifyUtils spotifyUtils = new SpotifyUtils();
  /**
   * Redirects a user to Spotify's web authentication page in order to get API access tokens
   */
  @RequestMapping(value = "/authenticate_spotify", method = RequestMethod.GET)
  public void authenticateWithSpotify(HttpServletResponse httpServletResponse,
      final @CookieValue(value = "refresh_token", defaultValue = "") String refreshToken) {
    if (refreshToken.equals("")) {
      // Generate a random state string and save in a cookie for verification
      final String state = UUID.randomUUID().toString();
      setServerCookie("state", state, httpServletResponse);

      httpServletResponse.setHeader("Location", buildSpotifyAuthorizationLink(state));
      httpServletResponse.setStatus(302);
    } else {
      try {
        System.out.println("Found existing cookie. No need to request from Spotify");
        httpServletResponse.sendRedirect("/");
      } catch (IOException e) {
        System.out.println("Ran into IO Exception while redirecting authenticated user to mainpage.");
      }
    }
  }

  /**
   * Endpoint called by Spotify after user completes authorization. Recieves an auth code from
   * Spotify that can be exchanged for an access token and refresh token. Makes call to Spotify to
   * fetch access token using auth code. Sets cookies with access and refresh tokens.
   */
  @RequestMapping(value = SPOTIFY_AUTH_CALLBACK, method = RequestMethod.GET)
  public ModelAndView spotifyAuthCallback(
      final @CookieValue(value = "state", defaultValue = "") String storedState,
      final @RequestParam(name = "code", required = false, defaultValue = "") String code,
      final @RequestParam(name = "state", required = true, defaultValue = "") String state,
      final @RequestParam(name = "error", required = false, defaultValue = "") String error,
      final @NonNull Model model,
      final HttpServletResponse httpServletResponse
  ) {

    //Removing state cookie
    clearCookie("state", httpServletResponse);

    if (!storedState.equals(state)) {
      System.out.println("Stored state does not match state returned from Spotify");
      addStandardSpotifyAuthErrorToModel(model);
    } else if (!code.equals("")) {
      if (spotifyUtils.fetchAccessTokenFromAuthCode(code, model, httpServletResponse)) {
        return new ModelAndView("index");
      }
    } else {
      System.out.println("Authorization failed. Error message: " + error);
      addStandardSpotifyAuthErrorToModel(model);
    }
    return new ModelAndView("redirect:/error", "error", model.getAttribute("error"));
  }
}

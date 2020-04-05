package com.example.spotifysync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
@SpringBootApplication
public class Application {
  private final String SPOTIFY_CLIENT_ID_VAR_KEY = "SPOTIFY_CLIENT_ID";

  @GetMapping("/")
  public String home(
      @RequestParam(name = "name", required = false, defaultValue = "World") String name,
      Model model) {
    model.addAttribute("name", name);
    return "index";
  }

  @RequestMapping(value = "/authenticate", method = RequestMethod.GET)
  public void authenticateWithSpotify(HttpServletResponse httpServletResponse) {
		final String state = UUID.randomUUID().toString();
		Cookie stateCookie = new Cookie("state", state);
		stateCookie.setPath("/");
		stateCookie.setSecure(true);
		stateCookie.setHttpOnly(true);

		// your application requests authorization
    final String responseType = "code";
    final String scope = "user-read-currently-playing";
    final String clientId = getSpotifyClientId();
    final String redirectUri = getServerUrl() + "callback";
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

	@RequestMapping(value = "/callback", method = RequestMethod.GET)
	public String callback(
			@CookieValue(value = "state", defaultValue = "") String storedState,
			@RequestParam(name = "code", required = false, defaultValue = "") String code,
			@RequestParam(name = "state", required = true, defaultValue = "") String state,
			@RequestParam(name = "error", required = false, defaultValue = "") String error,
			Model model,
			HttpServletResponse httpServletResponse
	) {

  	//Removing state cookie
		Cookie stateCookie = new Cookie("state", null);
		stateCookie.setMaxAge(0);
		stateCookie.setSecure(true);
		stateCookie.setHttpOnly(true);
		stateCookie.setPath("/");
		//add cookie to response
		httpServletResponse.addCookie(stateCookie);

  	if (!storedState.equals(state)) {
  		System.out.println("Stored state does not match state returned from Spotify");
			System.out.println("Authorization failed. States do not match.");
		}
  	if (!code.equals(""))  {
  		System.out.println("Authorization successful. Auth Token: " + code);
			model.addAttribute("name", "token: " + code);
		} else {
  		System.out.println("Authorization failed. Error messge: " + error);
			model.addAttribute("name", "failure: " + error);
		}
		return "index";
	}

  private String getSpotifyClientId() {
    return System.getenv(SPOTIFY_CLIENT_ID_VAR_KEY);
  }

  private String getServerUrl() {
    return "https://spotify-youtube.herokuapp.com/";
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

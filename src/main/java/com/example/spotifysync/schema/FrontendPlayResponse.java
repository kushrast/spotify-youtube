package com.example.spotifysync.schema;

/**
 * Entity class to keep track of the data sent to the frontend for the sync endpoint
 */
public class FrontendPlayResponse {
  private String spotifyUri;
  private String youTubeId;
  private int progressSeconds;
  private boolean currentPlaying;
  private String error;

  public FrontendPlayResponse() {
  }

  public FrontendPlayResponse(String spotifyUri, String youTubeId, int progressSeconds,
      boolean currentPlaying) {
    this.spotifyUri = spotifyUri;
    this.youTubeId = youTubeId;
    this.progressSeconds = progressSeconds;
    this.currentPlaying = currentPlaying;
    this.error = "";
  }

  public FrontendPlayResponse(String error) {
    this.error = error;
  }

  public String getSpotifyUri() {
    return spotifyUri;
  }

  public String getYouTubeId() {
    return youTubeId;
  }

  public int getProgressSeconds() {
    return progressSeconds;
  }

  public boolean currentPlaying() {
    return currentPlaying;
  }

  public String getError() {
    return error;
  }
}

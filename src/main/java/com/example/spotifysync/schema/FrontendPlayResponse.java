package com.example.spotifysync.schema;

/**
 * Entity class to keep track of the data sent to the frontend for the sync endpoint
 */
@Entity
public class FrontendPlayResponse {
  private String spotifyUri;
  private String youTubeId;
  private int progressSeconds;
  private boolean isPlaying;
  private String error;

  public FrontendPlayResponse() {
  }

  public FrontendPlayResponse(String spotifyUri, String youTubeId, int progressSeconds,
      boolean isPlaying) {
    this.spotifyUri = spotifyUri;
    this.youTubeId = youTubeId;
    this.progressSeconds = progressSeconds;
    this.isPlaying = isPlaying;
    this.error = "";
  }

  public FrontendPlayResponse(String error) {
    this.error = error;
  }
}

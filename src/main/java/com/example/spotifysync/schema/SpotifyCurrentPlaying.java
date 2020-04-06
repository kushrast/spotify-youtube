package com.example.spotifysync.schema;

import java.util.List;

/**
 * Entity class holding data about the track currently playing in Spotify
 */
public class SpotifyCurrentPlaying {
  private int durationMs;
  private int progressMs;
  private String trackName;
  private List<String> authors;
  private boolean isPlaying;

  private boolean isEmpty;

  public SpotifyCurrentPlaying(int durationMs, int progressMs, String trackName,
      List<String> authors, boolean isPlaying) {
    this.durationMs = durationMs;
    this.progressMs = progressMs;
    this.trackName = trackName;
    this.authors = authors;
    this.isPlaying = isPlaying;
    this.isEmpty = false;
  }

  public SpotifyCurrentPlaying() {
    this.isEmpty = true;
  }

  public int getDurationMs() {
    return durationMs;
  }

  public int getProgressMs() {
    return progressMs;
  }

  public String getTrackName() {
    return trackName;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public boolean isPlaying() {
    return isPlaying;
  }

  public boolean isEmpty() {
    return isEmpty;
  }
}

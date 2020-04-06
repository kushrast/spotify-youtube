package com.example.spotifysync.schema;

import java.util.List;
import java.util.StringJoiner;

/**
 * Entity class holding data about the track currently playing in Spotify
 */
public class SpotifyCurrentPlaying {
  private int durationMs;
  private int progressMs;
  private String trackName;
  private List<String> artists;
  private boolean isPlaying;

  private boolean isEmpty;

  public SpotifyCurrentPlaying(int durationMs, int progressMs, String trackName,
      List<String> artists, boolean isPlaying) {
    this.durationMs = durationMs;
    this.progressMs = progressMs;
    this.trackName = trackName;
    this.artists = artists;
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

  public List<String> getArtists() {
    return artists;
  }

  public boolean isPlaying() {
    return isPlaying;
  }

  public boolean isEmpty() {
    return isEmpty;
  }

  public String getYouTubeSearchParams() {
    StringJoiner searchString = new StringJoiner(" ");
    searchString.add(trackName);

    for (String artist: artists) {
      searchString.add(artist);
    }

    return searchString.toString();
  }

  public int compareSpotifyTrackToYouTubeVideo(String youTubeTitle, String youTubeAuthor) {
    return 100;
  }

  @Override public String toString() {
    return "SpotifyCurrentPlaying{" +
        "durationMs=" + durationMs +
        ", progressMs=" + progressMs +
        ", trackName='" + trackName + '\'' +
        ", artists=" + artists +
        ", isPlaying=" + isPlaying +
        ", isEmpty=" + isEmpty +
        '}';
  }
}

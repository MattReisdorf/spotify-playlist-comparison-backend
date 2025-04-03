package com.mattreisdorf.spotify_playlist_comparison_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrackItem {
  @JsonProperty
  private Track track;

  public Track getTrack() {
    return track;
  }
  public void setTrack(Track track) {
    this.track = track;
  }
}

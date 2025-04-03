package com.mattreisdorf.spotify_playlist_comparison_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalUrls {
  @JsonProperty
  private String spotify;

  public String getSpotify() {
    return spotify;
  }
  public void setSpotify(String spotify) {
    this.spotify = spotify;
  }
}
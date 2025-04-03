package com.mattreisdorf.spotify_playlist_comparison_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Artist {
  @JsonProperty
  private String name;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
}

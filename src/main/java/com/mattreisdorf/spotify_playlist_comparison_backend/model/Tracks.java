package com.mattreisdorf.spotify_playlist_comparison_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tracks {
  @JsonProperty
  private int total;

  public int getTotal() {
    return total;
  }
  public void setTotal(int total) {
    this.total = total;
  }
}

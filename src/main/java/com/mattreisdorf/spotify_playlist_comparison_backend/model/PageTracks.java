package com.mattreisdorf.spotify_playlist_comparison_backend.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PageTracks {
  @JsonProperty
  private List<TrackItem> items;

  public List<TrackItem> getItems() {
    return items;
  }
  public void setItems(List<TrackItem> items) {
    this.items = items;
  }
}

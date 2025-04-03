package com.mattreisdorf.spotify_playlist_comparison_backend.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Album {
  @JsonProperty
  private String name;

  @JsonProperty
  private List<Object> images;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public List<Object> getImages() {
    return images;
  }
  public void setImages(List<Object> images) {
    this.images = images;
  }
}

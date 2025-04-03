package com.mattreisdorf.spotify_playlist_comparison_backend.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Track {
  @JsonProperty
  private String name;

  @JsonProperty
  private List<Artist> artists;

  @JsonProperty
  private Album album;

  @JsonProperty
  private ExternalUrls external_urls;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public List<Artist> getArtists() {
    return artists;
  }
  public void setArtists(List<Artist> artists) {
    this.artists = artists;
  }

  public Album getAlbum() {
    return album;
  }
  public void setAlbum(Album album) {
    this.album = album;
  }

  public ExternalUrls getExternalUrls() {
    return external_urls;
  }
  public void setExternalUrls(ExternalUrls external_urls) {
    this.external_urls = external_urls;
  }
}

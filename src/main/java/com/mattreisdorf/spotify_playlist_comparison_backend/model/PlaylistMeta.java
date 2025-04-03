package com.mattreisdorf.spotify_playlist_comparison_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlaylistMeta {
  @JsonProperty
  private Tracks tracks;

  @JsonProperty
  private String name;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public Tracks getTracks() {
    return tracks;
  }
  public void setTracks(Tracks tracks) {
    this.tracks = tracks;
  }



  // private Tracks tracks;

  // private String name;

  // public String getName() {
  //   return name;
  // }
  // public void setName(String name){
  //   this.name = name;
  // }

  // public Tracks getTracks() {
  //   return tracks;
  // }
  // public void setTracks(Tracks tracks) {
  //   this.tracks = tracks;
  // }
}






package com.mattreisdorf.spotify_playlist_comparison_backend.exception;

public class PlaylistNotFoundException extends RuntimeException {
  public PlaylistNotFoundException(String message) {
    super(message);
  }
}

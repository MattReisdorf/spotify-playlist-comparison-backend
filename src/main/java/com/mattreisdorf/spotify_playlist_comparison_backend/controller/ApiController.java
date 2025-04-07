package com.mattreisdorf.spotify_playlist_comparison_backend.controller;

import com.mattreisdorf.spotify_playlist_comparison_backend.service.SpotifyApiService;

import java.util.Map;

import javax.naming.NameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
public class ApiController {

  @Autowired
  private SpotifyApiService spotifyApiService;

  @GetMapping("/api/user")
  public ResponseEntity<String> getUserData(HttpServletRequest request) {
    try {
      HttpSession session = request.getSession(false);
      if (session == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authorized");
      }
      String userData = spotifyApiService.getCurrentUserData(session);
      return ResponseEntity.ok(userData);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @GetMapping("/api/playlist")
  public ResponseEntity<?> getPlaylistData(HttpServletRequest request, @RequestParam("playlist") String playlistUrl) {
    try {
      HttpSession session = request.getSession(false);
      if (session == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session has ended. Please reload the page");
      }

      Map<String, Object> playlistDetails = spotifyApiService.getPlaylistDetails(playlistUrl, session);
      return ResponseEntity.ok(playlistDetails);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (NameNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
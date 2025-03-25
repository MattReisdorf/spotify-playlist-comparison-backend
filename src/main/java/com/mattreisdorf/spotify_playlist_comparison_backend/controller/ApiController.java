package com.mattreisdorf.spotify_playlist_comparison_backend.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
public class ApiController {

  private final String SPOTIFY_USER_URL = "https://api.spotify.com/v1/me";
  private final String SPOTIFY_PLAYLIST_URL = "https://api.spotify.com/v1/playlists/";

  @GetMapping("/api/user")
  public ResponseEntity<String> getUserData(HttpServletRequest request) {

    HttpSession session = request.getSession(false);

    if (session == null || session.getAttribute("access_token") == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authorized");
    }

    String accessToken = (String) session.getAttribute("access_token");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> userDataResponse = restTemplate.exchange(
        SPOTIFY_USER_URL,
        HttpMethod.GET,
        entity,
        String.class);

    return ResponseEntity.status(userDataResponse.getStatusCode()).body(userDataResponse.getBody());
  }

  @GetMapping("/api/playlist")
  public ResponseEntity<String> getPlaylistData(
      HttpServletRequest request,
      @RequestParam("playlist") String playlistUrl) {

    String urlRegex = "spotify\\.com/playlist/([^/?]+)";
    Pattern pattern = Pattern.compile(urlRegex);
    Matcher matcher = pattern.matcher(playlistUrl);

    HttpSession session = request.getSession(false);

    if (session == null || session.getAttribute("access_token") == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authorized");
    }

    String accessToken = (String) session.getAttribute("access_token");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    if (matcher.find()) {
      String playlistId = matcher.group(1);

      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> playlistDataResponse = restTemplate.exchange(
          SPOTIFY_PLAYLIST_URL + playlistId,
          HttpMethod.GET,
          entity,
          String.class);
      return ResponseEntity.status(playlistDataResponse.getStatusCode()).body(playlistDataResponse.getBody());
    }

    return new ResponseEntity<>("Invalid Playlist", HttpStatus.OK);

  }
}

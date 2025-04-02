package com.mattreisdorf.spotify_playlist_comparison_backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.connector.Response;
import org.springframework.core.ParameterizedTypeReference;
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
import org.springframework.web.util.UriComponentsBuilder;

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
  public ResponseEntity<?> getPlaylistData(HttpServletRequest request, @RequestParam("playlist") String playlistUrl) {

    // TODO: ID vs URL checking

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

      UriComponentsBuilder builder = UriComponentsBuilder
          .fromUriString(SPOTIFY_PLAYLIST_URL + playlistId + "/tracks")
          .queryParam("limit", 1)
          .queryParam("offset", 0);

      ResponseEntity<Map> totalResponse = restTemplate.exchange(
          builder.toUriString(),
          HttpMethod.GET,
          entity,
          Map.class);

      int total = (int) totalResponse.getBody().get("total");

      List<Object> allItems = new ArrayList<>();
      int limit = 100;

      for (int offset = 0; offset < total; offset += limit) {
        UriComponentsBuilder pageBuilder = UriComponentsBuilder
            .fromUriString(SPOTIFY_PLAYLIST_URL + playlistId + "/tracks")
            .queryParam("limit", limit)
            .queryParam("offset", offset);

        ResponseEntity<Map> pageResponse = restTemplate.exchange(
            pageBuilder.toUriString(),
            HttpMethod.GET,
            entity,
            Map.class);

        List<Object> pageItems = (List<Object>) pageResponse.getBody().get("items");
        allItems.addAll(pageItems);

      }

      Map<String, Object> response = new HashMap<>();
      response.put("id", playlistId);
      response.put("total", total);
      response.put("tracks", allItems);

      return new ResponseEntity<>(response, HttpStatus.OK);

    }

    return new ResponseEntity<>("Invalid Playlist", HttpStatus.OK);

  }
}

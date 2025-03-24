package com.mattreisdorf.spotify_playlist_comparison_backend.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
public class ApiController {
  
  private final String SPOTIFY_USER_URL = "https://api.spotify.com/v1/me";

  @GetMapping("/api/user")
  public ResponseEntity<String> getUserData(HttpServletRequest request) {

    HttpSession session = request.getSession(false);

    if (session == null || session.getAttribute("access_token") == null) {
      System.out.println();
      System.out.println("Session is either null or access_token is null");
      System.out.println();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authorized");
    }

    System.out.println();
    System.out.println(session.getAttribute("access_token"));
    System.out.println();

    String accessToken = (String) session.getAttribute("access_token");

    HttpHeaders headers = new HttpHeaders();
    // headers.set("Authorization", "Bearer " + accessToken);
    headers.setBearerAuth(accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> spotifyResponse = restTemplate.exchange(
      SPOTIFY_USER_URL,
      HttpMethod.GET,
      entity,
      String.class
    );

    return ResponseEntity.status(spotifyResponse.getStatusCode()).body(spotifyResponse.getBody());
  }

  // @GetMapping("/api/playlist/{playlist_id}")
  // public ResponseEntity<String> getPlaylistData(HttpServletRequest request, @PathVariable String playlistId) {
  //   HttpSession session = request.getSession(false);

  //   if (session == null || session.getAttribute("access_token") == null) {
  //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authorized");
  //   }

  //   String accessToken = (String) session.getAttribute("access_token");

  //   HttpHeaders headers = new HttpHeaders();
  //   headers.setBearerAuth(accessToken);

  //   HttpEntity<String> entity = new HttpEntity<>(headers);

  //   RestTemplate restTemplate = new RestTemplate();

  //   ResponseEntity<String> spotifyReponse = restTemplate.exchange(

  //   )
  // }
}

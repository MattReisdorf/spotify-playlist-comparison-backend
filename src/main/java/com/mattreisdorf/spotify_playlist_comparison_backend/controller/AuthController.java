package com.mattreisdorf.spotify_playlist_comparison_backend.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.mattreisdorf.spotify_playlist_comparison_backend.model.SpotifyTokenResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 
// Logic for OAuth2 authentication with Spotify
// 

@RestController
public class AuthController {

  // Environment Variables
  @Value("${spotify.client.id}")
  private String clientId;

  @Value("${spotify.client.secret}")
  private String clientSecret;

  @Value("${spotify.redirect.uri}")
  private String redirectUri;

  private final String AUTHORIZE_URL = "https://accounts.spotify.com/authorize";
  private final String TOKEN_URL = "https://accounts.spotify.com/api/token";
  

  // Begin authorization process with this endpoint
  @GetMapping("/login")
  public void login(HttpServletResponse response) throws IOException {
    // Generate state to protect against CSRF
    String state = UUID.randomUUID().toString();

    // Define access scope
    String scope = "user-read-private user-read-email";

    // Create url for starting auth
    String url = AUTHORIZE_URL + "?client_id=" + clientId
        + "&response_type=code"
        + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
        + "&scope=" + URLEncoder.encode(scope, "UTF-8")
        + "&state=" + state;

    // Redirect to auth endpoint
    response.sendRedirect(url);
  }

  // Callback route after authorization
  @SuppressWarnings("null")
  @GetMapping("/callback")
  public void callback(
      @RequestParam("code") String code,
      @RequestParam("state") String state,
      HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    String auth = clientId + ":" + clientSecret;

    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    headers.set("Authorization", "Basic " + encodedAuth);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    // Set up form parameters for token exchange
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("code", code);
    params.add("redirect_uri", redirectUri);

    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

    // Exchange authorization code for tokens
    ResponseEntity<SpotifyTokenResponse> tokenResponseEntity = restTemplate.postForEntity(TOKEN_URL, requestEntity, SpotifyTokenResponse.class);

    // Set up user session if callback returns OK and body
    if (tokenResponseEntity.getStatusCode() == HttpStatus.OK && tokenResponseEntity.getBody() != null) {
      SpotifyTokenResponse tokenResponse = tokenResponseEntity.getBody();
      HttpSession session = request.getSession(true);
      session.setAttribute("access_token", tokenResponse.getAccessToken());
      session.setAttribute("refresh_token", tokenResponse.getRefreshToken());
      response.sendRedirect("http://localhost:3000/");
    } else {
      response.sendRedirect("http://localhost:3000/error");
    }

  }
}

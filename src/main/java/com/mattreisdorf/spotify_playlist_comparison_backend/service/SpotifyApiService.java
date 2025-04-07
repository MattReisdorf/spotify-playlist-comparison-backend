package com.mattreisdorf.spotify_playlist_comparison_backend.service;

import com.mattreisdorf.spotify_playlist_comparison_backend.model.PageTracks;
import com.mattreisdorf.spotify_playlist_comparison_backend.model.PlaylistMeta;
import com.mattreisdorf.spotify_playlist_comparison_backend.model.TrackItem;
import com.mattreisdorf.spotify_playlist_comparison_backend.util.ApiUtils;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import javax.naming.NameNotFoundException;

@Service
public class SpotifyApiService {

  private final String SPOTIFY_USER_URL = "https://api.spotify.com/v1/me";
  private final String SPOTIFY_PLAYLIST_URL = "https://api.spotify.com/v1/playlists/";

  private final RestTemplate restTemplate;

  public SpotifyApiService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String getCurrentUserData(HttpSession session) {
    String accessToken = (String) session.getAttribute("access_token");
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(
        SPOTIFY_USER_URL,
        HttpMethod.GET,
        entity,
        String.class);

    return response.getBody();
  }

  public Map<String, Object> getPlaylistDetails(String playlistUrl, HttpSession session) throws NameNotFoundException {
    String playlistId = ApiUtils.extractIdString(playlistUrl);
    if (!ApiUtils.validateIdString(playlistId)) {
      throw new IllegalArgumentException("Invalid Playlist (Must be full URL or ID with characters 0-9, a-z, A-Z)");
    }

    String accessToken = (String) session.getAttribute("access_token");
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    RestTemplate restTemplate = new RestTemplate();

    PlaylistMeta playlistMeta;
    String playlistName;
    int total;
    List<TrackItem> allTracks = new ArrayList<>();
    int limit = 100;

    try {
      playlistMeta = restTemplate.exchange(
          SPOTIFY_PLAYLIST_URL + playlistId,
          HttpMethod.GET,
          entity,
          new ParameterizedTypeReference<PlaylistMeta>() {
          })
          .getBody();

      if (playlistMeta == null) {
        throw new RuntimeException("Playlist metadata was null or malformed");
      }

      playlistName = playlistMeta.getName();
      total = playlistMeta.getTracks().getTotal();

    } catch (Exception e) {
      throw new NameNotFoundException("Playlist Not Found");
    }

    try {
      for (int offset = 0; offset < total; offset += limit) {
        UriComponentsBuilder pageBuilder = UriComponentsBuilder
            .fromUriString(SPOTIFY_PLAYLIST_URL + playlistId + "/tracks")
            .queryParam("limit", limit)
            .queryParam("offset", offset);

        ResponseEntity<PageTracks> pageResponse = restTemplate.exchange(
            pageBuilder.toUriString(),
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<PageTracks>() {
            });

        PageTracks pageItems = pageResponse.getBody();
        if (pageItems == null) {
          throw new RuntimeException("Paginated Tracks Response Was Unexpectedly Null");
        }
        allTracks.addAll(pageItems.getItems());
      }

      Map<String, Object> responseBody = new HashMap<>();
      responseBody.put("id", playlistId);
      responseBody.put("name", playlistName);
      responseBody.put("total", total);
      responseBody.put("tracks", allTracks);

      return responseBody;

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
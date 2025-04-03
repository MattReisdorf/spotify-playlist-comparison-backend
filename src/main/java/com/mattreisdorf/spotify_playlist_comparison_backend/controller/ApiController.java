package com.mattreisdorf.spotify_playlist_comparison_backend.controller;

import com.mattreisdorf.spotify_playlist_comparison_backend.model.PageTracks;
import com.mattreisdorf.spotify_playlist_comparison_backend.model.PlaylistMeta;
import com.mattreisdorf.spotify_playlist_comparison_backend.model.TrackItem;
import com.mattreisdorf.spotify_playlist_comparison_backend.util.ApiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    String playlistId = ApiUtils.extractIdString(playlistUrl);
    boolean validId = ApiUtils.validateIdString(playlistId);

    HttpSession session;
    try {
      session = request.getSession(false);
    } catch (Exception e) {
      return new ResponseEntity<>("Not Authorized", HttpStatus.UNAUTHORIZED);
    }

    String accessToken = (String) session.getAttribute("access_token");
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<String> entity = new HttpEntity<>(headers);

    if (validId) {
      RestTemplate restTemplate = new RestTemplate();

      PlaylistMeta playlistMeta;
      String playlistName;
      int total;
      List<TrackItem> allTracks = new ArrayList<>();
      int limit = 100;
      
      try {
        ResponseEntity<PlaylistMeta> playlistMetaResponse = restTemplate.exchange(
          SPOTIFY_PLAYLIST_URL + playlistId,
          HttpMethod.GET,
          entity,
          new ParameterizedTypeReference<PlaylistMeta>() {}
        );

        // Metadata *should* never be null
        // As long as ID is valid, response *should* have a body, even if playlist doesn't have any tracks
        // If something tragic happens, this will throw a null pointer exception
        // But that really shouldn't happen
        playlistMeta = Objects.requireNonNull(playlistMetaResponse.getBody(), "Playlist metadata was unexpectedly null");
        playlistName = playlistMeta.getName();
        total = playlistMeta.getTracks().getTotal();




      } catch (NullPointerException npe) {
        // I really don't expect this to ever happen
        // In the absolutley apocalpytic case that it does, this will handle it
        return new ResponseEntity<>("Playlist metadata was null or malformed", HttpStatus.INTERNAL_SERVER_ERROR);
      } catch (Exception e) {
        // Catch any other exceptions that happen here
        // Should be a valid (ie base62), but bad ID
        return new ResponseEntity<>("Caught Exception in Playlist Metadata", HttpStatus.BAD_REQUEST);
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
            new ParameterizedTypeReference<PageTracks>() {}
            );

          PageTracks pageItems =  Objects.requireNonNull(pageResponse.getBody(), "Paginated Tracks Response Was Unexpectedly Null");
          allTracks.addAll(pageItems.getItems());

          
        }

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", playlistId);
        responseBody.put("name", playlistName);
        responseBody.put("total", total);
        responseBody.put("tracks", allTracks);

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
      } catch (NullPointerException npe) {
        return new ResponseEntity<>("Paginated Tracks Response Was Unexpectedly Null", HttpStatus.INTERNAL_SERVER_ERROR);
      } catch (Exception e) {
        return new ResponseEntity<>("Caught Exception in All Tracks", HttpStatus.BAD_REQUEST);
      }
    }

    return new ResponseEntity<>("Invalid Playlist", HttpStatus.OK);

  }
}

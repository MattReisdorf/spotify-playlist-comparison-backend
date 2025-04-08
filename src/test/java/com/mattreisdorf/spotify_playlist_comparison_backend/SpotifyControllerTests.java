package com.mattreisdorf.spotify_playlist_comparison_backend;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mattreisdorf.spotify_playlist_comparison_backend.controller.ApiController;
import com.mattreisdorf.spotify_playlist_comparison_backend.exception.PlaylistNotFoundException;
import com.mattreisdorf.spotify_playlist_comparison_backend.service.SpotifyApiService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiController.class)
public class SpotifyControllerTests {
  
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SpotifyApiService spotifyApiService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testGetPlaylistData_Success() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("access_token", "mock_token");

    Map<String, Object> mockResponse = Map.of(
      "id", "mockid",
      "name", "mockname",
      "total", 2,
      "tracks", List.of(Map.of("track", "a"), Map.of("track", "b"))
    );

    Mockito.when(spotifyApiService.getPlaylistDetails(eq("https://open.spotify.com/playlist/mockid"), any())).thenReturn(mockResponse);

    mockMvc.perform(get("/api/playlist")
      .param("playlist", "https://open.spotify.com/playlist/mockid")
      .session(session))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value("mockid"))
      .andExpect(jsonPath("$.name").value("mockname"))
      .andExpect(jsonPath("$.total").value(2))
      .andExpect(jsonPath("$.tracks").isArray());
  }

  @Test
  void testGetPlaylistData_UnauthorizedFailure() throws Exception {
    mockMvc.perform(get("/api/playlist")
    .param("playlist", "https://open.spotify.com/playlist/mockid"))
    .andExpect(status().isUnauthorized())
    .andExpect(content().string("Session has ended. Please reload the page."));
  }

  @Test
  void testGetPlaylistData_IllegalArgumentFailure() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("access_token", "mock_token");

    String invalidPlaylistUrl = "https://open.spotify.com/playlist/!nv4l!d!d";

    when(spotifyApiService.getPlaylistDetails(eq(invalidPlaylistUrl), any()))
      .thenThrow(new IllegalArgumentException("Invalid Playlist ID"));

    mockMvc.perform(get("/api/playlist")
    .param("playlist", invalidPlaylistUrl)
    .session(session))
    .andExpect(status().isBadRequest())
    .andExpect(content().string("Invalid Playlist ID"));
  }

  @Test
  void testGetPlaylistData_PlaylistNotFoundFailure() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("access_token", "mock_token");

    String unfoundPlaylistUrl = "https://open.spotify.com/playlist/notfound";

    when(spotifyApiService.getPlaylistDetails(eq(unfoundPlaylistUrl), any()))
      .thenThrow(new PlaylistNotFoundException("Playlist Not Found"));

    mockMvc.perform(get("/api/playlist")
    .param("playlist", unfoundPlaylistUrl)
    .session(session))
    .andExpect(status().isNotFound())
    .andExpect(content().string("Playlist Not Found"));
  }

  @Test
  void testGetPlaylistData_InternalServerErrorFailure() throws Exception {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("access_token", "mock_token");

    String playlistUrl = "https://open.spotify.com/playlist/playlistid";

    when(spotifyApiService.getPlaylistDetails(eq(playlistUrl), any()))
      .thenThrow(new RuntimeException());

      mockMvc.perform(get("/api/playlist")
      .param("playlist", playlistUrl)
      .session(session))
      .andExpect(status().isInternalServerError());
    }
}
package com.mattreisdorf.spotify_playlist_comparison_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mattreisdorf.spotify_playlist_comparison_backend.exception.PlaylistNotFoundException;
import com.mattreisdorf.spotify_playlist_comparison_backend.model.PageTracks;
import com.mattreisdorf.spotify_playlist_comparison_backend.model.PlaylistMeta;
import com.mattreisdorf.spotify_playlist_comparison_backend.model.TrackItem;
import com.mattreisdorf.spotify_playlist_comparison_backend.model.Tracks;
import com.mattreisdorf.spotify_playlist_comparison_backend.service.SpotifyApiService;

import jakarta.servlet.http.HttpSession;

@SpringBootTest
class SpotifyServiceTests {

	@InjectMocks
	private SpotifyApiService spotifyApiService;

	@Mock
	private RestTemplate mockRestTemplate;

	@Mock
	private HttpSession mockSession;

	@Captor
	ArgumentCaptor<HttpEntity<String>> httpEntityCaptor;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		spotifyApiService = new SpotifyApiService(mockRestTemplate);
	}

	@Test
	void testGetCurrenUserData_Success() {
		when(mockSession.getAttribute("access_token")).thenReturn("mock_token");

		ResponseEntity<String> mockResponse = new ResponseEntity<>("User Data", HttpStatus.OK);
		when(mockRestTemplate.exchange(
			eq("https://api.spotify.com/v1/me"),
			eq(HttpMethod.GET),
			any(HttpEntity.class),
			eq(String.class)
			)).thenReturn(mockResponse);

		String result = spotifyApiService.getCurrentUserData(mockSession);
		assertEquals("User Data", result);
	}

	@Test
	void testGetCurrentUserData_Failure() {
		when(mockSession.getAttribute("access_token")).thenReturn("invalid_token");

		when(mockRestTemplate.exchange(
			anyString(),
			eq(HttpMethod.GET),
			any(HttpEntity.class),
			eq(String.class)
		)).thenThrow(new RuntimeException("Spotify API Error"));

		Exception exception = assertThrows(RuntimeException.class, () -> spotifyApiService.getCurrentUserData(mockSession));
		
		assertEquals("Spotify API Error", exception.getMessage());
	}

	@Test
	void testGetPlaylistData_Success() {
		String playlistUrl = "https://open.spotify.com/playlist/mockid";
		String playlistId = "mockid";

		when(mockSession.getAttribute("access_token")).thenReturn("mock_token");

		PlaylistMeta mockPlaylistMeta = new PlaylistMeta();
		mockPlaylistMeta.setName("mockName");
		Tracks mockTracks = new Tracks();
		mockTracks.setTotal(2);
		mockPlaylistMeta.setTracks(mockTracks);


		when(mockRestTemplate.exchange(
			eq("https://api.spotify.com/v1/playlists/" + playlistId),
			eq(HttpMethod.GET),
			any(HttpEntity.class),
			ArgumentMatchers.<ParameterizedTypeReference<PlaylistMeta>>any()
		)).thenReturn(new ResponseEntity<>(mockPlaylistMeta, HttpStatus.OK));

		PageTracks mockPage = new PageTracks();
		mockPage.setItems(List.of(new TrackItem(), new TrackItem()));

		when(mockRestTemplate.exchange(
			contains("/tracks"),
			eq(HttpMethod.GET),
			any(HttpEntity.class),
			ArgumentMatchers.<ParameterizedTypeReference<PageTracks>>any()
		)).thenReturn(new ResponseEntity<>(mockPage, HttpStatus.OK));

		Map<String, Object> mockResult = spotifyApiService.getPlaylistDetails(playlistUrl, mockSession);

		assertEquals("mockid", mockResult.get("id"));
		assertEquals("mockName", mockResult.get("name"));
		assertEquals(2, mockResult.get("total"));
		assertEquals(2, ((List<?>) mockResult.get("tracks")).size());
	}

	@Test
	void testGetPlaylistData_MultiPageSuccess() {
		String playlistUrl = "https://open.spotify.com/playlist/mockid";
		String playlistId = "mockid";

		when(mockSession.getAttribute("access_token")).thenReturn("mock_token");

		PlaylistMeta mockPlaylistMeta = new PlaylistMeta();
		mockPlaylistMeta.setName("mockName");
		Tracks mockTracks = new Tracks();
		mockTracks.setTotal(150);
		mockPlaylistMeta.setTracks(mockTracks);

		when(mockRestTemplate.exchange(
			eq("https://api.spotify.com/v1/playlists/" + playlistId),
			eq(HttpMethod.GET),
			any(HttpEntity.class),
			ArgumentMatchers.<ParameterizedTypeReference<PlaylistMeta>>any()
		)).thenReturn(new ResponseEntity<>(mockPlaylistMeta, HttpStatus.OK));

		PageTracks mockFirstPage = new PageTracks();
		mockFirstPage.setItems(Collections.nCopies(100, new TrackItem()));

		when(mockRestTemplate.exchange(
			contains("/tracks"),
			eq(HttpMethod.GET),
			any(HttpEntity.class),
			ArgumentMatchers.<ParameterizedTypeReference<PageTracks>>any()
		)).thenReturn(new ResponseEntity<>(mockFirstPage, HttpStatus.OK));

		PageTracks mockSecondPage = new PageTracks();
    mockSecondPage.setItems(Collections.nCopies(50, new TrackItem()));

    when(mockRestTemplate.exchange(
        contains("offset=100"),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        ArgumentMatchers.<ParameterizedTypeReference<PageTracks>>any()
    )).thenReturn(new ResponseEntity<>(mockSecondPage, HttpStatus.OK));

		Map<String, Object> mockResult = spotifyApiService.getPlaylistDetails(playlistUrl, mockSession);

		assertEquals("mockid", mockResult.get("id"));
    assertEquals("mockName", mockResult.get("name"));
    assertEquals(150, mockResult.get("total"));
    assertEquals(150, ((List<?>) mockResult.get("tracks")).size());
	}

	@Test
	void testGetPlaylistData_IllegalArgumentFailure() {
		String playlistUrl = "https://open.spotify.com/playlist/!nv4l!d!d";

		when(mockSession.getAttribute("acess_token")).thenReturn("mock_token");

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			spotifyApiService.getPlaylistDetails(playlistUrl, mockSession);
		});
		
		assertEquals("Invalid Playlist (Must be full URL or ID with characters 0-9, a-z, A-Z)", exception.getMessage());
	}

	@Test
	void testGetPlaylistData_PlaylistNotFoundFailure() {
		String playlistUrl = "https://open.spotify.com/playlist/mockid";
		String playlistId = "mockid";

		when(mockSession.getAttribute("acess_token")).thenReturn("mock_token");

		ResponseEntity<PlaylistMeta> mockNullMetaResponse = new ResponseEntity<>(null, HttpStatus.OK);

		when(mockRestTemplate.exchange(
        eq("https://api.spotify.com/v1/playlists/" + playlistId),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        ArgumentMatchers.<ParameterizedTypeReference<PlaylistMeta>>any()
    )).thenReturn(mockNullMetaResponse);

		Exception exception = assertThrows(PlaylistNotFoundException.class, () ->
        spotifyApiService.getPlaylistDetails(playlistUrl, mockSession)
    );

		assertEquals("Playlist Not Found", exception.getMessage());
	}

}

package com.mattreisdorf.spotify_playlist_comparison_backend.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiUtils {
  private ApiUtils() {
    // Private contstructor to prevent instantiation
  }
  
  public static String extractIdString(String input) {
    String urlRegex = "spotify\\.com/playlist/([^/?]+)";
    String base62Regex = "^[A-Za-z0-9]+$";
    Pattern urlPattern = Pattern.compile(urlRegex);
    Pattern base62Pattern = Pattern.compile(base62Regex);
    Matcher urlMatcher = urlPattern.matcher(input);
    Matcher base62Matcher = base62Pattern.matcher(input);
    
    if (urlMatcher.find()) {
      return urlMatcher.group(1);
    } else if (base62Matcher.find()) {
      return base62Matcher.group(0);
    }
    return input;
  }

  public static boolean validateIdString(String input) {
    String base62Regex = "^[A-Za-z0-9]+$";
    Pattern base62Pattern = Pattern.compile(base62Regex);
    Matcher base62Matcher = base62Pattern.matcher(input);
    if (base62Matcher.find()) {
      return true;
    }
    return false;
  }
}

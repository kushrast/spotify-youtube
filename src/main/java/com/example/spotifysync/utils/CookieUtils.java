package com.example.spotifysync.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Utils class to help get and set cookies
 */
public class CookieUtils {
  public static void setServerCookie(final String key, final String value,
      final HttpServletResponse response) {
    final Cookie cookie = new Cookie(key, value);
    cookie.setPath("/");
    cookie.setSecure(true);
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
  }

  public static void setCookie(final String key, final String value, final HttpServletResponse response) {
    setCookie(key, value, Integer.MAX_VALUE, response);
  }

  public static void setCookie(final String key, final String value, final int expiryTime,
      final HttpServletResponse response) {
    final Cookie cookie = new Cookie(key, value);
    cookie.setPath("/");
    cookie.setSecure(true);
    cookie.setMaxAge(expiryTime);
    response.addCookie(cookie);
  }

  public static void clearCookie(final String key, final HttpServletResponse response) {
    final Cookie cookie = new Cookie(key, null);
    cookie.setMaxAge(0);
    cookie.setSecure(true);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    //add cookie to response
    response.addCookie(cookie);
  }
}

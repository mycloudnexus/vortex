package com.consoleconnect.vortex.core.exception;

import lombok.Getter;

@Getter
public class VortexException extends RuntimeException {

  private static final String NOT_FOUND_MSG = "404 Not Found";
  private static final String NOT_FOUND_DESC =
      "The requested URL was not found on the server. If you entered the URL manually please check your spelling and try again.";

  private final int code;

  public VortexException(int code) {
    this.code = code;
  }

  public VortexException(int code, String message) {
    super(message);
    this.code = code;
  }

  public VortexException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public VortexException(int code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public static VortexException badRequest(String message) {
    return new VortexException(400, message);
  }

  public static VortexException notFound(String message) {
    return new VortexException(404, message);
  }

  public static VortexException notFound(String message, Throwable cause) {
    return new VortexException(404, message, cause);
  }

  public static VortexException notFoundDefault() {
    return new VortexException(404, NOT_FOUND_MSG, new IllegalArgumentException(NOT_FOUND_DESC));
  }

  public static VortexException unauthorized(String message) {
    return new VortexException(401, message);
  }

  public static VortexException forbidden(String message) {
    return new VortexException(403, message);
  }

  public static VortexException internalError(String message) {
    return new VortexException(500, message);
  }

  public static VortexException notImplemented(String message) {
    return new VortexException(501, message);
  }
}

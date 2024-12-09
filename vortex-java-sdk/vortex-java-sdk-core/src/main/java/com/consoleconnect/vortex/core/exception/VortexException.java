package com.consoleconnect.vortex.core.exception;

import java.io.Serial;
import lombok.Generated;
import org.springframework.http.HttpStatus;

public class VortexException extends RuntimeException {
  @Serial private static final long serialVersionUID = 2322270398663247466L;
  private final int code;

  public static VortexException badRequest(String message) {
    return new VortexException(HttpStatus.BAD_REQUEST.value(), message);
  }

  public static VortexException badRequest(String message, Throwable cause) {
    return new VortexException(HttpStatus.BAD_REQUEST.value(), message, cause);
  }

  public static VortexException notFound(String message) {
    return new VortexException(HttpStatus.NOT_FOUND.value(), message);
  }

  public static VortexException notFound() {
    return new VortexException(HttpStatus.NOT_FOUND.value(), "Resource not found");
  }

  public static VortexException notFound(String message, Throwable cause) {
    return new VortexException(HttpStatus.NOT_FOUND.value(), message, cause);
  }

  public static VortexException unauthorized(String message) {
    return new VortexException(HttpStatus.UNAUTHORIZED.value(), message);
  }

  public static VortexException unauthorized(String message, Throwable cause) {
    return new VortexException(HttpStatus.UNAUTHORIZED.value(), message, cause);
  }

  public static VortexException forbidden(String message) {
    return new VortexException(HttpStatus.FORBIDDEN.value(), message);
  }

  public static VortexException forbidden(String message, Throwable cause) {
    return new VortexException(HttpStatus.FORBIDDEN.value(), message, cause);
  }

  public static VortexException internalError(String message) {
    return new VortexException(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
  }

  public static VortexException internalError(String message, Throwable cause) {
    return new VortexException(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, cause);
  }

  public static VortexException notImplemented(String message) {
    return new VortexException(HttpStatus.NOT_IMPLEMENTED.value(), message);
  }

  public static VortexException notImplemented(String message, Throwable cause) {
    return new VortexException(HttpStatus.NOT_IMPLEMENTED.value(), message, cause);
  }

  public static VortexException exception(int code, String message, Throwable cause) {
    return new VortexException(code, message, cause);
  }

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

  @Generated
  public int getCode() {
    return this.code;
  }
}

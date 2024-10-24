package com.consoleconnect.vortex.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class VortexExceptionAdvice {

  public static final String DELIMITER = System.lineSeparator();

  @ExceptionHandler(value = {VortexException.class})
  ResponseEntity<VortexError> handleVortexException(VortexException e, HttpServletRequest request) {
    this.logError(request, e.getCode(), e.getMessage());
    return new ResponseEntity<>(
        VortexError.builder().code(e.getCode()).error(e.getMessage()).build(),
        HttpStatus.valueOf(e.getCode()));
  }

  @ExceptionHandler(value = {BindException.class})
  ResponseEntity<VortexError> handleBindException(BindException e, HttpServletRequest request) {
    this.logError(request, 400, e);
    return new ResponseEntity<>(
        VortexError.builder().code(400).error(e.getMessage()).build(), HttpStatus.valueOf(400));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<VortexError> handleNoResourceFoundException(
      NoResourceFoundException e, HttpServletRequest request) {
    this.logError(request, e.getStatusCode().value(), e);
    return new ResponseEntity<>(
        VortexError.builder().code(e.getStatusCode().value()).error(e.getMessage()).build(),
        HttpStatus.valueOf(e.getStatusCode().value()));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<VortexError> handleConstraintViolationException(
      ConstraintViolationException e, HttpServletRequest request) {
    this.logError(request, 400, e);
    return new ResponseEntity<>(
        VortexError.builder().code(HttpStatus.BAD_REQUEST.value()).error(e.getMessage()).build(),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<VortexError> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e, HttpServletRequest request) {
    this.logError(request, 400, e);
    return new ResponseEntity<>(
        VortexError.builder().code(HttpStatus.BAD_REQUEST.value()).error(e.getMessage()).build(),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<VortexError> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
    this.logError(request, 400, e);
    return new ResponseEntity<>(
        VortexError.builder().code(HttpStatus.BAD_REQUEST.value()).error(e.getMessage()).build(),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<VortexError> handleGenericException(
      Exception e, HttpServletRequest request) {
    log.info("Unhandled exception: {}", e.getClass());
    this.logError(request, 500, e);
    return new ResponseEntity<>(
        VortexError.builder().code(500).error(e.getMessage()).build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private void logError(HttpServletRequest request, int statusCode, String message) {
    log.error(
        "[{}]{} {},error:{}", statusCode, request.getMethod(), request.getRequestURI(), message);
  }

  private void logError(HttpServletRequest request, int statusCode, Exception e) {
    log.error(
        "[{}]{} {},error:{}",
        statusCode,
        request.getMethod(),
        request.getRequestURI(),
        e.getMessage(),
        e);
  }
}

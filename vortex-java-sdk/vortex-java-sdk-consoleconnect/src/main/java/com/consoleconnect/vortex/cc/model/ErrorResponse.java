package com.consoleconnect.vortex.cc.model;

import lombok.Data;

@Data
public class ErrorResponse {
  private Error error;

  @Data
  public static class Error {
    private int statusCode;
    private String name;
    private String message;
  }
}

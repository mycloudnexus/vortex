package com.consoleconnect.vortex.cc;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleConnectBearerTokenInterceptor implements RequestInterceptor {

  private final String token;

  public ConsoleConnectBearerTokenInterceptor(String token) {
    this.token = token;
  }

  @Override
  public void apply(RequestTemplate template) {
    template.header("Authorization", token);
  }
}

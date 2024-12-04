package com.consoleconnect.vortex.cc;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleConnectBearerTokenInterceptor implements RequestInterceptor {

  private final String token;

  private static final String TOKEN_PREFIX = "Bearer ";
  private static final String TOKEN_HEADER_NAME = "Authorization";

  public ConsoleConnectBearerTokenInterceptor(String token) {
    this.token = token;
  }

  @Override
  public void apply(RequestTemplate template) {

    if (token != null) {
      if (token.startsWith(TOKEN_PREFIX)) {
        template.header(TOKEN_HEADER_NAME, token);
      } else {
        template.header(TOKEN_HEADER_NAME, TOKEN_PREFIX + token);
      }
    }
  }
}

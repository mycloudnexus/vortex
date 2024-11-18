package com.consoleconnect.vortex.cc;

import feign.Feign;
import feign.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

public final class ConsoleConnectClientFactory {

  private ConsoleConnectClientFactory() {
    // prevent instantiation
  }

  public static ConsoleConnectClient create(
      String baseUrl, String accessToken, Logger.Level logLevel) {
    ObjectFactory<HttpMessageConverters> messageConverters = HttpMessageConverters::new;
    return Feign.builder()
        .requestInterceptor(new ConsoleConnectBearerTokenInterceptor(accessToken))
        .contract(new SpringMvcContract())
        .encoder(new SpringEncoder(messageConverters))
        .decoder(new SpringDecoder(messageConverters))
        .errorDecoder(new ConsoleConnectErrorDecoder())
        .logger(new ConsoleConnectSlf4jLogger())
        .logLevel(logLevel)
        .target(ConsoleConnectClient.class, baseUrl);
  }

  public static ConsoleConnectClient create(String baseUrl, String accessToken) {
    return create(baseUrl, accessToken, Logger.Level.BASIC);
  }
}

package com.consoleconnect.vortex.test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

public class WebTestClientHelper {

  private WebTestClient webTestClient;

  public WebTestClientHelper(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }

  public <T> T getAndVerify(
      Function<UriBuilder, URI> uriFunction,
      ParameterizedTypeReference<T> responseType,
      Consumer<T> verify) {
    return requestAndVerify(
        HttpMethod.GET, uriFunction, HttpStatus.OK.value(), null, responseType, verify);
  }

  public <T> T getAndVerify(
      Function<UriBuilder, URI> uriFunction,
      HttpStatus httpStatus,
      ParameterizedTypeReference<T> responseType,
      Consumer<T> verify) {
    return requestAndVerify(
        HttpMethod.GET, uriFunction, httpStatus.value(), null, responseType, verify);
  }

  public <T> T postAndVerify(
      Function<UriBuilder, URI> uriFunction,
      ParameterizedTypeReference<T> responseType,
      Consumer<T> verify) {
    return requestAndVerify(
        HttpMethod.POST, uriFunction, HttpStatus.OK.value(), null, responseType, verify);
  }

  public <T> T postAndVerify(
      Function<UriBuilder, URI> uriFunction,
      Object body,
      ParameterizedTypeReference<T> responseType,
      Consumer<T> verify) {
    return requestAndVerify(
        HttpMethod.POST, uriFunction, HttpStatus.OK.value(), body, responseType, verify);
  }

  public <T> T postAndVerify(
      Function<UriBuilder, URI> uriFunction,
      HttpStatus httpStatus,
      Object body,
      ParameterizedTypeReference<T> responseType,
      Consumer<T> verify) {
    return requestAndVerify(
        HttpMethod.POST, uriFunction, httpStatus.value(), body, responseType, verify);
  }

  public <T> T patchAndVerify(
      Function<UriBuilder, URI> uriFunction,
      Object body,
      ParameterizedTypeReference<T> responseType,
      Consumer<T> verify) {
    return requestAndVerify(
        HttpMethod.PATCH, uriFunction, HttpStatus.OK.value(), body, responseType, verify);
  }

  public <T> T deleteAndVerify(
      Function<UriBuilder, URI> uriFunction,
      ParameterizedTypeReference<T> responseType,
      Consumer<T> verify) {
    return requestAndVerify(
        HttpMethod.DELETE, uriFunction, HttpStatus.OK.value(), null, responseType, verify);
  }

  public <T> T requestAndVerify(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      int statusCode,
      Object body,
      ParameterizedTypeReference<T> responseType,
      Consumer<T> verify) {
    return requestAndVerify(method, uriFunction, null, statusCode, body, responseType, verify);
  }

  public <T> T requestAndVerify(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      Map<String, String> headers,
      int statusCode,
      Object body,
      ParameterizedTypeReference<T> responseType,
      Consumer<T> verify) {
    WebTestClient.RequestBodySpec requestBodySpec =
        webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(600))
            .build()
            .method(method)
            .uri(uriFunction)
            .header("content-type", "application/json");
    if (body != null) {
      requestBodySpec.bodyValue(body);
    }
    if (headers != null) {
      headers.forEach(requestBodySpec::header);
    }
    return requestBodySpec
        .exchange()
        .expectStatus()
        .isEqualTo(statusCode)
        .expectBody(responseType)
        .consumeWith(
            response -> {
              if (verify != null) {
                T res = response.getResponseBody();
                verify.accept(res);
              }
            })
        .returnResult()
        .getResponseBody();
  }
}

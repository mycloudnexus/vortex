package com.consoleconnect.vortex.core.toolkit;

import com.consoleconnect.vortex.core.exception.VortexException;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@AllArgsConstructor
public class GenericHttpClient {
  private final WebClient client;

  public WebClient.ResponseSpec curl(
      String url, HttpMethod method, Map<String, String> httpHeaders, Object body) {
    log.info("method:{},url:{}", method, url);

    WebClient.RequestBodySpec resSpec =
        client
            .method(method)
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);
    if (MapUtils.isNotEmpty(httpHeaders)) {
      httpHeaders.entrySet().stream().forEach(h -> resSpec.header(h.getKey(), h.getValue()));
    }

    if (body != null) {
      return resSpec.bodyValue(body).retrieve();
    } else {
      return resSpec.retrieve();
    }
  }

  public <T> T blockPut(
      String url,
      Map<String, String> headers,
      Object body,
      ParameterizedTypeReference<T> responseType) {
    return curl(url, HttpMethod.PUT, headers, body).bodyToMono(responseType).block();
  }

  public <T> T unblockGet(
      String url,
      Map<String, String> headers,
      Object body,
      ParameterizedTypeReference<T> responseType) {
    try {
      WebClient.ResponseSpec responseSpec = curl(url, HttpMethod.GET, headers, body);
      return responseSpec.bodyToMono(responseType).toFuture().get();
    } catch (InterruptedException e) {
      log.warn("unblockGet.interrupted", e);
      Thread.currentThread().interrupt(); // Suggested by SonarCloud.
      throw VortexException.badRequest("interrupted error, " + e.getMessage());
    } catch (Exception e) {
      log.error("unknown.error", e);
      throw VortexException.badRequest("get error, " + e.getMessage());
    }
  }
}

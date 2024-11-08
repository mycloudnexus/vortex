package com.consoleconnect.vortex.iam.service;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@AllArgsConstructor
public class VortexServerConnector {
  private WebClient client;

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
      log.debug("object-request-body:{}", JsonToolkit.toJson(body));
      return resSpec.bodyValue(body).retrieve();
    } else {
      return resSpec.retrieve();
    }
  }

  public <T> T put(
      String url,
      Map<String, String> headers,
      Object body,
      ParameterizedTypeReference<T> responseType) {
    return curl(url, HttpMethod.PUT, headers, body).bodyToMono(responseType).block();
  }
}

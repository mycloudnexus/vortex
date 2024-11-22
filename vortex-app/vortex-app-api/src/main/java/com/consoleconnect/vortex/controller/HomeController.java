package com.consoleconnect.vortex.controller;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author dxiong
 */
@Hidden
@RestController
@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "General")
public class HomeController {

  @Value("${app.iam.downstream.api-spec:classpath:/openapi/downstream-api-spec.json}")
  private String downstreamApiSpecPath;

  @GetMapping
  public Mono<Void> index(ServerHttpResponse response) {
    response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
    response.getHeaders().setLocation(URI.create("/swagger-ui.html"));
    return response.setComplete();
  }

  @GetMapping(value = "/v3/api-docs/downstream")
  public Mono<Object> downstreamAPISpec() throws IOException {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(downstreamApiSpecPath);
    String data = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
    return Mono.just(JsonToolkit.fromJson(data, Object.class));
  }
}

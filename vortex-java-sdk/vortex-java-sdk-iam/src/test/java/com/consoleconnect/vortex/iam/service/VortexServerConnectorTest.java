package com.consoleconnect.vortex.iam.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.consoleconnect.vortex.config.TestApplication;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class)
class VortexServerConnectorTest {
  private WebClient webClient = mock(WebClient.class);

  @Test
  void curl() {
    GenericHttpClient vortexServerConnector = new GenericHttpClient(webClient);

    WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    doReturn(requestBodyUriSpec).when(webClient).method(HttpMethod.PUT);

    doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(anyString());

    doReturn(requestBodyUriSpec).when(requestBodyUriSpec).accept(MediaType.APPLICATION_JSON);
    doReturn(requestBodyUriSpec).when(requestBodyUriSpec).contentType(any());

    WebClient.RequestBodyUriSpec resSpec = mock(WebClient.RequestBodyUriSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
    doReturn(responseSpec).when(resSpec).retrieve();

    Mono<Map> mapMono = mock(Mono.class);
    doReturn(mapMono).when(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));
    doReturn(Map.of()).when(mapMono).block();

    vortexServerConnector.curl("http://lcaolhost", HttpMethod.PUT, null, null);
    Assertions.assertThatNoException();
  }
}

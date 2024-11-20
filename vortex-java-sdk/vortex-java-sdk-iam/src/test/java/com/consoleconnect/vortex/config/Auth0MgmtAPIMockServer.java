package com.consoleconnect.vortex.config;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.http.HttpMethod;

public class Auth0MgmtAPIMockServer {

  @SneakyThrows
  public static void setupMock() {
    String indexJson = AbstractIntegrationTest.readFileToString("auth0/index.json");

    List<MockData> dataList =
        JsonToolkit.fromJson(indexJson, new TypeReference<List<MockData>>() {});
    for (MockData data : dataList) {
      System.out.println(data);
      String jsonData = AbstractIntegrationTest.readFileToString("auth0/" + data.data);
      WireMock.stubFor(
          WireMock.request(data.method.name(), urlPathTemplate(data.endpoint))
              .willReturn(
                  WireMock.aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(jsonData)));
    }
  }

  public static void verify(int count, String url, String accessToken) {
    WireMock.verify(
        count,
        getRequestedFor(urlEqualTo(url))
            .withHeader("Authorization", equalTo("Bearer " + accessToken)));
  }

  public static void verify(int count, String url) {

    WireMock.verify(count, getRequestedFor(urlEqualTo(url)).withHeader("Authorization", absent()));
  }

  @Data
  public static class MockData {
    private HttpMethod method;
    private String endpoint;
    private String data;
    private boolean endpointTemplate;
  }
}

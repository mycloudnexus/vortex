package com.consoleconnect.vortex.test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

@Slf4j
public class MockServerHelper {

  private MockServerHelper() {}

  public static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    return objectMapper;
  }

  @SneakyThrows
  public static void setupMock(String mockDataPath) {
    String indexJson = AbstractIntegrationTest.readFileToString(mockDataPath + "/index.json");

    List<MockData> dataList =
        createObjectMapper().readValue(indexJson, new TypeReference<List<MockData>>() {});
    for (MockData data : dataList) {
      log.info("{}", data);
      String jsonData = AbstractIntegrationTest.readFileToString(mockDataPath + "/" + data.data);
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
    verify(count, HttpMethod.GET, url, accessToken);
  }

  public static void verify(int count, String url) {
    verify(count, HttpMethod.GET, url, null);
  }

  public static void verify(int count, HttpMethod httpMethod, String url) {
    verify(count, httpMethod, url, null);
  }

  public static void verify(int count, HttpMethod httpMethod, String url, String accessToken) {
    WireMock.verify(
        count,
        WireMock.requestedFor(httpMethod.name(), urlEqualTo(url))
            .withHeader(
                "Authorization",
                accessToken == null ? absent() : equalTo("Bearer " + accessToken)));
  }

  @Data
  public static class MockData {
    private HttpMethod method;
    private String endpoint;
    private String data;
  }
}

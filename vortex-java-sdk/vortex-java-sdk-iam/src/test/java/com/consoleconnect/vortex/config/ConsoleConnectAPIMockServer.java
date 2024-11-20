package com.consoleconnect.vortex.config;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;

public class ConsoleConnectAPIMockServer {
  @SneakyThrows
  public static void setupMock() {
    String members = AbstractIntegrationTest.readFileToString("consoleconnect/members.json");
    WireMock.stubFor(
        WireMock.get(urlPathTemplate("/v2/companies/{companyId}/members"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(members)));
    String memberInfo = AbstractIntegrationTest.readFileToString("consoleconnect/member_info.json");
    WireMock.stubFor(
        WireMock.get(urlPathTemplate("/api/user/{username}"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(memberInfo)));
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
}

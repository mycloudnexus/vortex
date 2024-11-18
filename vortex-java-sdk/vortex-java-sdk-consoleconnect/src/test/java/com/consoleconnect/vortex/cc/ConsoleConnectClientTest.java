package com.consoleconnect.vortex.cc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.consoleconnect.vortex.cc.model.Heartbeat;
import com.consoleconnect.vortex.cc.model.Member;
import com.consoleconnect.vortex.cc.model.UserInfo;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@WireMockTest
public class ConsoleConnectClientTest {

  public static final String ACCESS_TOKEN = UUID.randomUUID().toString();

  public static String readFileToString(String path) throws IOException {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(path);
    return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
  }

  @BeforeAll
  public static void setUp() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.DEBUG);
  }

  @Test
  void test_getHeartbeat_returnOk(WireMockRuntimeInfo wireMockRuntimeInfo) throws IOException {
    String jsonData = readFileToString("data/heartbeat.json");
    WireMock.stubFor(
        WireMock.get(urlEqualTo("/heartbeat"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonData)));

    // access-token is not required to query heartbeat
    Heartbeat res =
        ConsoleConnectClientFactory.create(wireMockRuntimeInfo.getHttpBaseUrl(), null)
            .getHeartbeat();
    Assertions.assertNotNull(res);
    Assertions.assertNotNull(res.getVersion());
    verify(1, getRequestedFor(urlEqualTo("/heartbeat")).withHeader("Authorization", absent()));
  }

  @Test
  void test_getCurrentUser_returnOk(WireMockRuntimeInfo wireMockRuntimeInfo) throws IOException {

    String jsonData = readFileToString("data/current-user.json");
    WireMock.stubFor(
        WireMock.get(urlEqualTo("/api/auth/token"))
            .withHeader("Authorization", WireMock.equalTo("Bearer " + ACCESS_TOKEN))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonData)));

    // access-token is required to query current user
    UserInfo res =
        ConsoleConnectClientFactory.create(
                wireMockRuntimeInfo.getHttpBaseUrl(), ACCESS_TOKEN, feign.Logger.Level.FULL)
            .getCurrentUser();
    Assertions.assertNotNull(res);
    Assertions.assertNotNull(res.getName());
    Assertions.assertNotNull(res.getId());
    Assertions.assertNotNull(res.getUsername());
    Assertions.assertNotNull(res.getEmail());

    verify(
        1,
        getRequestedFor(urlEqualTo("/api/auth/token"))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN)));
  }

  @Test
  void test_getCurrentUser_return401(WireMockRuntimeInfo wireMockRuntimeInfo) throws IOException {

    String jsonData = readFileToString("data/401-unauthorized.json");
    WireMock.stubFor(
        WireMock.get(urlEqualTo("/api/auth/token"))
            .withHeader("Authorization", WireMock.not(WireMock.equalTo("Bearer " + ACCESS_TOKEN)))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(401)
                    .withStatusMessage("Unauthorized")
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonData)));
    WireMock.stubFor(
        WireMock.get(urlEqualTo("/api/auth/token"))
            .withHeader("Authorization", WireMock.absent())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(401)
                    .withStatusMessage("Unauthorized")
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonData)));

    // access via wrong token
    String randomToken = UUID.randomUUID().toString();
    VortexException exception =
        Assertions.assertThrowsExactly(
            VortexException.class,
            () -> {
              ConsoleConnectClientFactory.create(wireMockRuntimeInfo.getHttpBaseUrl(), randomToken)
                  .getCurrentUser();
            });
    verify(
        1,
        getRequestedFor(urlEqualTo("/api/auth/token"))
            .withHeader("Authorization", equalTo("Bearer " + randomToken)));
    Assertions.assertNotNull(exception);
    Assertions.assertEquals(401, exception.getCode());

    exception =
        Assertions.assertThrowsExactly(
            VortexException.class,
            () -> {
              ConsoleConnectClientFactory.create(wireMockRuntimeInfo.getHttpBaseUrl(), null)
                  .getCurrentUser();
            });
    Assertions.assertNotNull(exception);
    Assertions.assertEquals(401, exception.getCode());
    verify(1, getRequestedFor(urlEqualTo("/api/auth/token")).withHeader("Authorization", absent()));
  }

  @Test
  void test_listCompanyMembers_returnOk(WireMockRuntimeInfo wireMockRuntimeInfo)
      throws IOException {

    String companyId = UUID.randomUUID().toString();

    String url = String.format("/v2/companies/%s/members?pageSize=0", companyId);
    String jsonData = readFileToString("data/members.json");
    WireMock.stubFor(
        WireMock.get(urlEqualTo(url))
            .withHeader("Authorization", WireMock.equalTo("Bearer " + ACCESS_TOKEN))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonData)));

    // access-token is required to list company's members
    List<Member> res =
        ConsoleConnectClientFactory.create(wireMockRuntimeInfo.getHttpBaseUrl(), ACCESS_TOKEN)
            .listMembers(companyId);
    Assertions.assertNotNull(res);
    Assertions.assertFalse(res.isEmpty());

    verify(
        1,
        getRequestedFor(urlEqualTo(url))
            .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN)));
  }

  @Test
  void test_listCompanyMembers_return401(WireMockRuntimeInfo wireMockRuntimeInfo)
      throws IOException {

    String jsonData = readFileToString("data/401-unauthorized.json");
    String companyId = UUID.randomUUID().toString();
    String url = String.format("/v2/companies/%s/members?pageSize=0", companyId);
    WireMock.stubFor(
        WireMock.get(urlEqualTo(url))
            .withHeader("Authorization", WireMock.not(WireMock.equalTo("Bearer " + ACCESS_TOKEN)))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(401)
                    .withStatusMessage("Unauthorized")
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonData)));
    WireMock.stubFor(
        WireMock.get(urlEqualTo(url))
            .withHeader("Authorization", WireMock.absent())
            .willReturn(
                WireMock.aResponse()
                    .withStatus(401)
                    .withStatusMessage("Unauthorized")
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonData)));

    // access via wrong token
    String randomToken = UUID.randomUUID().toString();
    VortexException exception =
        Assertions.assertThrowsExactly(
            VortexException.class,
            () -> {
              ConsoleConnectClientFactory.create(wireMockRuntimeInfo.getHttpBaseUrl(), randomToken)
                  .listMembers(companyId);
            });
    verify(
        1,
        getRequestedFor(urlEqualTo(url))
            .withHeader("Authorization", equalTo("Bearer " + randomToken)));
    Assertions.assertNotNull(exception);
    Assertions.assertEquals(401, exception.getCode());

    exception =
        Assertions.assertThrowsExactly(
            VortexException.class,
            () -> {
              ConsoleConnectClientFactory.create(wireMockRuntimeInfo.getHttpBaseUrl(), null)
                  .listMembers(companyId);
            });
    Assertions.assertNotNull(exception);
    Assertions.assertEquals(401, exception.getCode());
    verify(1, getRequestedFor(urlEqualTo(url)).withHeader("Authorization", absent()));
  }

  @Test
  void test_getHeartbeat_return500(WireMockRuntimeInfo wireMockRuntimeInfo) {

    WireMock.stubFor(
        WireMock.get(urlEqualTo("/heartbeat"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(500)
                    .withStatusMessage("Service Unavailable")
                    .withBody("Internal Server Error")));

    // access via wrong token
    VortexException exception =
        Assertions.assertThrowsExactly(
            VortexException.class,
            () -> {
              ConsoleConnectClientFactory.create(wireMockRuntimeInfo.getHttpBaseUrl(), null)
                  .getHeartbeat();
            });
    verify(1, getRequestedFor(urlEqualTo("/heartbeat")));
    Assertions.assertNotNull(exception);
    Assertions.assertEquals(500, exception.getCode());
  }
}

package com.consoleconnect.vortex.test.user;

import com.consoleconnect.vortex.test.AuthContextConstants;
import com.consoleconnect.vortex.test.WebTestClientHelper;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriBuilder;

@Getter
public class TestUser {

  private WebTestClientHelper webTestClientHelper;

  private String accessToken;

  public Optional<String> requestAndVerify(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      int statusCode,
      Consumer<String> verify) {
    return requestAndVerify(method, uriFunction, null, statusCode, verify);
  }

  public Optional<String> requestAndVerify(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      Object body,
      int statusCode,
      Consumer<String> verify) {
    Map<String, String> headers = new HashMap<>();
    if (getAccessToken() != null) {
      headers.put("Authorization", "Bearer " + getAccessToken());
    }
    return getWebTestClientHelper()
        .requestAndVerify(method, uriFunction, headers, body, statusCode, verify);
  }

  public static TestUser login(
      final WebTestClientHelper webTestClientHelper, final String accessToken) {
    return new TestUser() {
      @Override
      public String getAccessToken() {
        return accessToken;
      }

      @Override
      public WebTestClientHelper getWebTestClientHelper() {
        return webTestClientHelper;
      }
    };
  }

  public static TestUser loginAsMgmtUser(final WebTestClientHelper webTestClientHelper) {
    return login(webTestClientHelper, AuthContextConstants.MGMT_ACCESS_TOKEN);
  }

  public static TestUser loginAsCustomerUser(final WebTestClientHelper webTestClientHelper) {
    return login(webTestClientHelper, AuthContextConstants.CUSTOMER_ACCESS_TOKEN);
  }

  public static TestUser anonymous(final WebTestClientHelper webTestClientHelper) {
    return login(webTestClientHelper, null);
  }
}

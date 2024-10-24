package com.consoleconnect.vortex.iam.client;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.TokenRequest;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.model.AppProperty;
import com.consoleconnect.vortex.iam.model.Auth0Config;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class Auth0Client {
  private AuthAPI authClient;
  private Auth0Config auth0Config;
  private Auth0HttpClient httpClient;
  private static final String TOKEN_URL_TEMPLATE = "https://%s/api/v2/";

  @Autowired
  public Auth0Client(AppProperty appProperty) {
    this.auth0Config = appProperty.getAuth().getAuth0();
    this.httpClient = DefaultHttpClient.newBuilder().build();
    this.authClient =
        AuthAPI.newBuilder(
                auth0Config.getDomain(), auth0Config.getClientId(), auth0Config.getClientSecret())
            .withHttpClient(httpClient)
            .build();
  }

  public ManagementAPI getMgmtClient() {
    String accessToken = getToken();
    return ManagementAPI.newBuilder(auth0Config.getDomain(), accessToken)
        .withHttpClient(httpClient)
        .build();
  }

  private String getToken() {
    try {
      TokenRequest tokenRequest =
          authClient.requestToken(String.format(TOKEN_URL_TEMPLATE, auth0Config.getDomain()));
      TokenHolder holder = tokenRequest.execute().getBody();
      return holder.getAccessToken();
    } catch (Exception e) {
      log.error("[module-auth]Error calling Auth0", e);
      throw VortexException.badRequest("Error calling Auth" + e.getMessage());
    }
  }
}

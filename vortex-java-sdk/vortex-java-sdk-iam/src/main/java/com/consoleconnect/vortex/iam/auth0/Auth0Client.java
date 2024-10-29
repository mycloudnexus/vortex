package com.consoleconnect.vortex.iam.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.TokenRequest;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.model.Auth0Property;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Auth0Client {
  @Getter private final Auth0Property auth0Property;
  private final AuthAPI auth0AuthAPI;
  private final Auth0HttpClient auth0HttpClient;
  private TokenHolder tokenHolder;

  public Auth0Client(Auth0Property auth0Property) {
    this.auth0Property = auth0Property;
    this.auth0HttpClient = DefaultHttpClient.newBuilder().build();
    this.auth0AuthAPI =
        AuthAPI.newBuilder(
                this.auth0Property.getMgmtApi().getDomain(),
                this.auth0Property.getMgmtApi().getClientId(),
                this.auth0Property.getMgmtApi().getClientSecret())
            .withHttpClient(auth0HttpClient)
            .build();
  }

  public ManagementAPI getMgmtClient() {
    return ManagementAPI.newBuilder(auth0Property.getMgmtApi().getDomain(), getAccessToken())
        .withHttpClient(auth0HttpClient)
        .build();
  }

  private String getAccessToken() {
    // Check if token is null or expired
    if (tokenHolder == null || new Date().after(tokenHolder.getExpiresAt())) {
      try {
        TokenRequest tokenRequest =
            auth0AuthAPI.requestToken(auth0Property.getMgmtApi().getAudience());
        tokenHolder = tokenRequest.execute().getBody();
      } catch (Exception e) {
        log.error("Error calling Auth0", e);
        throw VortexException.internalError("Error calling Auth" + e.getMessage());
      }
    }
    return tokenHolder.getAccessToken();
  }
}

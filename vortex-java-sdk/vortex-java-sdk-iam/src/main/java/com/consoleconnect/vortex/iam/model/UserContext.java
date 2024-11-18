package com.consoleconnect.vortex.iam.model;

import com.consoleconnect.vortex.cc.ConsoleConnectClient;
import com.consoleconnect.vortex.cc.ConsoleConnectClientFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {
  private Jwt jwt;
  private String subject;
  private String userId;
  private String orgId;
  private boolean mgmt;
  private String customerId;
  private ResourceServerProperty.TrustedIssuer trustedIssuer;
  private String apiServer;
  private String apiAccessToken;

  private ConsoleConnectClient consoleConnectClient;

  public ConsoleConnectClient getConsoleConnectClient() {
    if (consoleConnectClient != null) {
      return consoleConnectClient;
    }
    return ConsoleConnectClientFactory.create(apiServer, apiAccessToken);
  }
}

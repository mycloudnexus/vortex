package com.consoleconnect.vortex.iam.model;

import com.consoleconnect.vortex.cc.ConsoleConnectClient;
import com.consoleconnect.vortex.cc.ConsoleConnectClientFactory;
import com.consoleconnect.vortex.core.exception.VortexException;
import feign.Logger;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {
  private String userId;
  private String orgId;
  private boolean mgmt;
  private String customerId;
  private String apiServer;
  private List<String> roles;

  @ToString.Exclude private ResourceServerProperty.TrustedIssuer trustedIssuer;
  @ToString.Exclude private String accessToken;
  @ToString.Exclude private ConsoleConnectClient consoleConnectClient;

  public ConsoleConnectClient getConsoleConnectClient() {
    if (consoleConnectClient != null) {
      return consoleConnectClient;
    }
    if (apiServer == null || accessToken == null) {
      throw VortexException.badRequest("apiServer or apiAccessToken is null");
    }
    return ConsoleConnectClientFactory.create(apiServer, accessToken, Logger.Level.FULL);
  }
}

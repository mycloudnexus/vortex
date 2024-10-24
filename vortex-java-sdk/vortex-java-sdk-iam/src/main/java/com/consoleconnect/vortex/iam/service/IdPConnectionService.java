package com.consoleconnect.vortex.iam.service;

import com.auth0.json.mgmt.connections.Connection;
import com.auth0.net.Request;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.client.Auth0Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IdPConnectionService {
  private Auth0Client auth0Client;

  @Autowired
  public IdPConnectionService(Auth0Client auth0Client) {
    this.auth0Client = auth0Client;
  }

  public String create(Connection connection) {
    try {
      Request<Connection> response =
          this.auth0Client.getMgmtClient().connections().create(connection);
      return response.execute().getBody().getId();
    } catch (Exception e) {
      log.error("[module-auth]create.connection error", e);
      throw VortexException.badRequest("Create connection error" + e.getMessage());
    }
  }

  public Connection get(String connectionId) {
    try {
      Request<Connection> response =
          this.auth0Client.getMgmtClient().connections().get(connectionId, null);
      return response.execute().getBody();
    } catch (Exception e) {
      log.error("[module-auth]get.connection error", e);
      throw VortexException.badRequest("Get connection error" + e.getMessage());
    }
  }
}

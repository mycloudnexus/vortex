package com.consoleconnect.vortex.iam.service.connection;

import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
public abstract class AbstractConnectionProvider {
  protected final Auth0Client auth0Client;

  protected AbstractConnectionProvider(Auth0Client auth0Client) {
    this.auth0Client = auth0Client;
  }

  public abstract ConnectionStrategyEnum getConnectionStrategy();

  public abstract Map<String, Object> createConnectionOptions(CreateConnectionDto request);

  public abstract Map<String, Object> createConnectionOptions(
      Map<String, Object> options, UpdateConnectionDto request);

  public final Connection buildConnectionPayload(
      Organization organization, CreateConnectionDto request) {
    log.info("Building create connection payload for organization: {}", organization.getName());
    String name =
        String.format(
            "%s-%s-%s",
            organization.getName(),
            getConnectionStrategy().getValue(),
            RandomStringUtils.random(6, true, false));
    log.info("Connection name: {}", name);
    Connection connection = new Connection(name, getConnectionStrategy().getValue());
    connection.setEnabledClients(List.of(auth0Client.getAuth0Property().getApp().getClientId()));
    connection.setOptions(createConnectionOptions(request));
    return connection;
  }

  public final Connection buildConnectionPayload(
      Organization organization, Connection connection, UpdateConnectionDto dto) {
    log.info("Building update connection payload for organization: {}", organization.getName());
    Connection update = new Connection();
    update.setOptions(createConnectionOptions(connection.getOptions(), dto));
    return update;
  }

  public boolean assignMembershipOnLogin() {
    return true;
  }
}

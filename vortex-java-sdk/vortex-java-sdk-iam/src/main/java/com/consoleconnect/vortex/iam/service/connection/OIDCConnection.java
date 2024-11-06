package com.consoleconnect.vortex.iam.service.connection;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.OidcConnectionDto;
import com.consoleconnect.vortex.iam.dto.UpdateConnectionDto;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component("oidc")
public class OIDCConnection extends AbstractConnection {

  @Override
  Connection buildNewConnection(
      Organization organization,
      CreateConnectionDto createConnectionDto,
      ManagementAPI managementAPI) {

    Connection connection =
        new Connection(
            StringUtils.join(organization.getName(), "-", ConnectionStrategyEnum.OIDC.getValue()),
            ConnectionStrategyEnum.OIDC.getValue());
    connection.setEnabledClients(
        List.of(getAuth0Client().getAuth0Property().getApp().getClientId()));
    connection.setOptions(toMap(createConnectionDto.getOidc()));
    return connection;
  }

  @Override
  Connection buildUpdateConnection(
      Organization organization,
      Connection connection,
      UpdateConnectionDto updateConnectionDto,
      ManagementAPI managementAPI) {
    Connection update = new Connection();
    update.setOptions(toMap(updateConnectionDto.getOidc()));
    return update;
  }

  private Map<String, Object> toMap(OidcConnectionDto odic) {
    Map<String, Object> map = new HashMap<>();
    map.put("client_id", odic.getClientId());
    map.put("discovery_url", odic.getDiscoveryUrl());
    map.put("scope", odic.getScope());
    map.put("type", "front_channel");
    return map;
  }

  @Override
  ConnectionStrategyEnum getLoginType() {
    return ConnectionStrategyEnum.OIDC;
  }
}

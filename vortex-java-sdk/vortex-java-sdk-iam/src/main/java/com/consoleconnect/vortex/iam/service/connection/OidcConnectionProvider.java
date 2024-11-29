package com.consoleconnect.vortex.iam.service.connection;

import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.OidcConnectionDto;
import com.consoleconnect.vortex.iam.dto.UpdateConnectionDto;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OidcConnectionProvider extends AbstractConnectionProvider {

  @Autowired
  public OidcConnectionProvider(Auth0Client auth0Client) {
    super(auth0Client);
  }

  @Override
  public ConnectionStrategyEnum getConnectionStrategy() {
    return ConnectionStrategyEnum.OIDC;
  }

  @Override
  public Map<String, Object> createConnectionOptions(CreateConnectionDto request) {
    return toMap(request.getOidc());
  }

  @Override
  public Map<String, Object> createConnectionOptions(
      Map<String, Object> options, UpdateConnectionDto updateConnectionDto) {
    return toMap(updateConnectionDto.getOidc());
  }

  private Map<String, Object> toMap(OidcConnectionDto payload) {
    Map<String, Object> map = new HashMap<>();
    map.put("client_id", payload.getClientId());
    map.put("discovery_url", payload.getDiscoveryUrl());
    map.put("scope", payload.getScope());
    map.put("type", "front_channel");
    return map;
  }
}

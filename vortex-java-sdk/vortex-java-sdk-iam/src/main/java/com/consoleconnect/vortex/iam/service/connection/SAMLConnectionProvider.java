package com.consoleconnect.vortex.iam.service.connection;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.SamlConnectionDto;
import com.consoleconnect.vortex.iam.dto.UpdateConnectionDto;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SAMLConnectionProvider extends AbstractConnectionProvider {

  @Autowired
  public SAMLConnectionProvider(Auth0Client auth0Client) {
    super(auth0Client);
  }

  @Override
  public ConnectionStrategyEnum getConnectionStrategy() {
    return ConnectionStrategyEnum.SAML;
  }

  @Override
  public Map<String, Object> createConnectionOptions(CreateConnectionDto request) {
    SamlConnectionDto samlConnectionDto = request.getSaml();
    samlConnectionDto.setDebug(Boolean.TRUE);
    samlConnectionDto.setSignSAMLRequest(Boolean.FALSE);
    return JsonToolkit.fromJson(JsonToolkit.toJson(samlConnectionDto), new TypeReference<>() {});
  }

  @Override
  public Map<String, Object> createConnectionOptions(
      Map<String, Object> options, UpdateConnectionDto updateConnectionDto) {
    if (options == null) {
      options = new HashMap<>();
    }
    Map<String, Object> metaData =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(updateConnectionDto.getSaml()), new TypeReference<>() {});
    options.putAll(metaData);
    return options;
  }
}

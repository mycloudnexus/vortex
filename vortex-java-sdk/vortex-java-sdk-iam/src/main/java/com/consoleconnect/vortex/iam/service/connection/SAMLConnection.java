package com.consoleconnect.vortex.iam.service.connection;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.SamlConnectionDto;
import com.consoleconnect.vortex.iam.dto.UpdateConnectionDto;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component("samlp")
public class SAMLConnection extends AbstractConnection {

  @Override
  Connection buildNewConnection(
      Organization organization,
      CreateConnectionDto createConnectionDto,
      ManagementAPI managementAPI) {
    SamlConnectionDto samlConnectionDto = createConnectionDto.getSaml();
    samlConnectionDto.setDebug(Boolean.TRUE);
    samlConnectionDto.setSignSAMLRequest(Boolean.FALSE);
    Map<String, Object> metaData =
        JsonToolkit.fromJson(JsonToolkit.toJson(samlConnectionDto), new TypeReference<>() {});
    Connection connection =
        new Connection(
            StringUtils.join(organization.getName(), "-", ConnectionStrategyEnum.SAML.getValue()),
            ConnectionStrategyEnum.SAML.getValue());
    connection.setOptions(metaData);
    connection.setEnabledClients(
        List.of(getAuth0Client().getAuth0Property().getApp().getClientId()));
    return connection;
  }

  @Override
  Connection buildUpdateConnection(
      Organization organization,
      Connection connection,
      UpdateConnectionDto updateConnectionDto,
      ManagementAPI managementAPI) {
    Map<String, Object> metaData =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(updateConnectionDto.getSaml()), new TypeReference<>() {});
    Connection update = new Connection();
    update.setOptions(metaData);
    return update;
  }

  @Override
  ConnectionStrategyEnum getLoginType() {
    return ConnectionStrategyEnum.SAML;
  }
}

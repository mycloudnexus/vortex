package com.consoleconnect.vortex.iam.service.connection;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.SamlConnectionDto;
import com.consoleconnect.vortex.iam.dto.UpdateConnectionDto;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import com.consoleconnect.vortex.iam.service.ConnectionService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component("samlp")
public class SAMLConnection extends AbstractConnection {

  public SAMLConnection(Auth0Client auth0Client, ConnectionService connectionService) {
    super(auth0Client, connectionService);
  }

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
            StringUtils.join(organization.getName(), "-", ConnectionStrategryEnum.SAML.getValue()),
            ConnectionStrategryEnum.SAML.getValue());
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
    Map<String, Object> originalMeta = connection.getOptions();
    originalMeta.putAll(metaData);
    update.setOptions(originalMeta);
    return update;
  }
}

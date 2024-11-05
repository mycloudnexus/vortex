package com.consoleconnect.vortex.iam.service.connection;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import com.consoleconnect.vortex.iam.enums.LoginTypeEnum;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component("auth0")
public class UsernamePasswordConnection extends AbstractConnection {

  @Override
  Connection buildNewConnection(
      Organization organization,
      CreateConnectionDto createConnectionDto,
      ManagementAPI managementAPI) {

    Connection connection =
        new Connection(
            StringUtils.join(organization.getName(), "-", ConnectionStrategryEnum.AUTH0.getValue()),
            ConnectionStrategryEnum.AUTH0.getValue());
    Map<String, Object> options = new HashMap<>();
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", getEmailAttribute());
    options.put("attributes", attributes);
    options.put("disable_signup", false);

    connection.setOptions(options);
    connection.setEnabledClients(
        List.of(getAuth0Client().getAuth0Property().getApp().getClientId()));

    return connection;
  }

  private static Map<String, Object> getEmailAttribute() {
    Map<String, Object> attribute = new HashMap<>();
    attribute.put("identifier", Map.of("active", true));
    attribute.put("signup", Map.of("status", "required", "verification", Map.of("active", true)));
    return attribute;
  }

  @Override
  public void validateLoginType(Organization organization) {
    if (Objects.nonNull(organization.getMetadata())
        && organization
            .getMetadata()
            .get(META_LOGIN_TYPE)
            .equals(LoginTypeEnum.USERNAME_PASSWORD.name())) {
      throw VortexException.internalError(
          "Failed to create username-password connection of organization: " + organization.getId());
    }
  }

  @Override
  void canUpdateOnLoginType(String orgId, Organization organization) {
    if (Objects.nonNull(organization.getMetadata())
        && !LoginTypeEnum.USERNAME_PASSWORD
            .name()
            .equals(organization.getMetadata().get(META_LOGIN_TYPE))) {
      throw VortexException.internalError("Failed to change connections of organization: " + orgId);
    }
  }

  @Override
  boolean assignMembershipOnLogin() {
    // New users need to be invited.
    return Boolean.FALSE;
  }
}

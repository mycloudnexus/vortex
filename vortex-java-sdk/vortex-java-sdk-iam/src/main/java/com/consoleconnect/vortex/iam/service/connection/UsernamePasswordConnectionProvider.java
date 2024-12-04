package com.consoleconnect.vortex.iam.service.connection;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.CreateConnectionDto;
import com.consoleconnect.vortex.iam.dto.UpdateConnectionDto;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UsernamePasswordConnectionProvider extends AbstractConnectionProvider {

  @Autowired
  public UsernamePasswordConnectionProvider(Auth0Client auth0Client) {
    super(auth0Client);
  }

  @Override
  public ConnectionStrategyEnum getConnectionStrategy() {
    return ConnectionStrategyEnum.AUTH0;
  }

  @Override
  public Map<String, Object> createConnectionOptions(CreateConnectionDto request) {
    Map<String, Object> options = new HashMap<>();
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", getEmailAttribute());
    options.put("attributes", attributes);
    options.put("disable_signup", false);

    return options;
  }

  private static Map<String, Object> getEmailAttribute() {
    Map<String, Object> attribute = new HashMap<>();
    attribute.put("identifier", Map.of("active", true));
    attribute.put("signup", Map.of("status", "required", "verification", Map.of("active", true)));
    return attribute;
  }

  @Override
  public boolean assignMembershipOnLogin() {
    // New users need to be invited.
    return false;
  }

  @Override
  public Map<String, Object> createConnectionOptions(
      Map<String, Object> options, UpdateConnectionDto updateConnectionDto) {
    throw VortexException.badRequest("Don't support update.");
  }
}

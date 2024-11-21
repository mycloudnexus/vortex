package com.consoleconnect.vortex.iam.dto;

import com.auth0.json.mgmt.connections.Connection;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class OrganizationConnection {
  @JsonProperty("assign_membership_on_login")
  private boolean assignMembershipOnLogin;

  @JsonProperty("connection_id")
  private String connectionId;

  @JsonProperty("show_as_button")
  private Boolean showAsButton;

  private Connection connection;

  private Map<String, Object> link = new HashMap<>();
}

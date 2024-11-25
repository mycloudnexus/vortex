package com.consoleconnect.vortex.iam.dto;

import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.organizations.Branding;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrganizationInfo {

  @JsonProperty("id")
  private String id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("display_name")
  private String displayName;

  @JsonProperty("metadata")
  private OrganizationMetadata metadata;

  @JsonProperty("branding")
  private Branding branding;

  private Connection connection;
}

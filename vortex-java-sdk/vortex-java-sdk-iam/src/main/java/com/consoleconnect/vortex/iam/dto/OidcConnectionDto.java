package com.consoleconnect.vortex.iam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OidcConnectionDto {

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("discovery_url")
  private String discoveryUrl;

  private String scope = "openid profile email";
}

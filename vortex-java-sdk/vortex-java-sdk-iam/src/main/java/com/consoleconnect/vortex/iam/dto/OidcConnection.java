package com.consoleconnect.vortex.iam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class OidcConnection {

  @JsonProperty("client_id")
  private String clientId;

  @JsonProperty("discovery_url")
  private String discoveryUrl;

  private String scope = "openid profile email";

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("client_id", getClientId());
    map.put("discovery_url", getDiscoveryUrl());
    map.put("scope", getScope());
    map.put("type", "front_channel");
    return map;
  }
}

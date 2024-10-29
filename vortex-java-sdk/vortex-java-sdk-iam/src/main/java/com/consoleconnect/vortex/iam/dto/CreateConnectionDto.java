package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.ConnectionStrategryEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class CreateConnectionDto {

  private String name;
  private ConnectionStrategryEnum strategy = ConnectionStrategryEnum.OIDC;
  private Oidc odic;

  @Data
  public static class Oidc {
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
}

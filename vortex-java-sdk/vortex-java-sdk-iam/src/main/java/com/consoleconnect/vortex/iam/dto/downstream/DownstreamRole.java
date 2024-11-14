package com.consoleconnect.vortex.iam.dto.downstream;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class DownstreamRole {
  private String id;
  private String name;
  private String displayName;
  private String description;
  private Map<String, Boolean> permissions;
  private List<DownstreamPolicy> policies;
  private List<String> policyIds;

  @Data
  public static class DownstreamPolicy {
    private String id;
    private String name;
    private String description;
    private PolicyDefinition definition;
  }

  @Data
  public static class PolicyDefinition {
    @JsonProperty("Statement")
    private List<PolicyStatement> statement;
  }

  @Data
  public static class PolicyStatement {
    @JsonProperty("Action")
    private List<String> action;

    @JsonProperty("Resource")
    private List<String> resource;

    @JsonProperty("Effect")
    private String effect;

    @JsonProperty("Sid")
    private String sid;
  }
}

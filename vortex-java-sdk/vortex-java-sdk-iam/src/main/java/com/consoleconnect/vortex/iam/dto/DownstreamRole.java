package com.consoleconnect.vortex.iam.dto;

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
  private List<Map<String, Object>> policies;
  private List<String> policyIds;
}

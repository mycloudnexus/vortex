package com.consoleconnect.vortex.iam.dto;

import java.util.Map;
import lombok.Data;

@Data
public class DownstreamRole {
  private String name;
  private String displayName;
  private Map<String, Boolean> permissions;
}

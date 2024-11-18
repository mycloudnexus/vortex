package com.consoleconnect.vortex.iam.dto;

import java.util.List;
import lombok.Data;

@Data
public class AuthToken {
  private String userId;
  private String orgId;
  private List<String> roles;

  private Object userInfo;
}

package com.consoleconnect.vortex.iam.dto.downstream;

import lombok.Data;

@Data
public class BaseUserInfo {
  private String id;
  private String username;
  private String name;
  private String email;
}

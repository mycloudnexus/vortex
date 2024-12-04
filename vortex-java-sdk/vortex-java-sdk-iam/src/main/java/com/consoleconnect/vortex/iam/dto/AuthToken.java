package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.UserTypeEnum;
import java.util.List;
import lombok.Data;

@Data
public class AuthToken {
  private String userId;
  private String name;
  private String email;

  private String orgId;
  private List<String> roles;
  private UserTypeEnum userType;

  private Object userInfo;
}

package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.cc.model.UserInfo;
import com.consoleconnect.vortex.core.model.AbstractModel;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractModel {
  private String username;
  private String name;
  private String email;
  private List<String> roles;
  private String organizationId;

  private UserInfo.LinkUserCompany organization;
}

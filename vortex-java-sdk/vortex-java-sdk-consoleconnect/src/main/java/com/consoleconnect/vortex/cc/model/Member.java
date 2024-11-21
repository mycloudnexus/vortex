package com.consoleconnect.vortex.cc.model;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseUserInfo {
  private List<Role> roles;
}

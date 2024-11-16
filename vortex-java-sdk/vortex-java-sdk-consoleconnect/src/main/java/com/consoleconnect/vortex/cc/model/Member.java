package com.consoleconnect.vortex.cc.model;

import java.util.List;
import lombok.Data;

@Data
public class Member extends BaseUserInfo {
  private List<Role> roles;
}

package com.consoleconnect.vortex.gateway.dto;

import com.consoleconnect.vortex.gateway.enums.AccessActionEnum;
import lombok.Data;

@Data
public class UpdatePathAccessRuleRequest {
  private String method;
  private String path;
  private AccessActionEnum action;
}

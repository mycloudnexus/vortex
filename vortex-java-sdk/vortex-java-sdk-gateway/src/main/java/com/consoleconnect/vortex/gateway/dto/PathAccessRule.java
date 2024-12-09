package com.consoleconnect.vortex.gateway.dto;

import com.consoleconnect.vortex.core.model.AbstractModel;
import com.consoleconnect.vortex.gateway.enums.AccessActionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PathAccessRule extends AbstractModel {
  private String method;
  private String path;
  private AccessActionEnum action;
}

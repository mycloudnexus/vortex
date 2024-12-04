package com.consoleconnect.vortex.gateway.dto;

import com.consoleconnect.vortex.core.model.AbstractModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Resource extends AbstractModel {
  private String customerId;
  private String orderId;
  private String resourceId;
  private String resourceType;
}

package com.consoleconnect.vortex.gateway.dto;

import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceRequest {
  private String customerId;
  private ResourceTypeEnum resourceType;
  private String orderId;
  private String resourceId;
}

package com.consoleconnect.vortex.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceRequest {
  private String customerId;
  private String resourceType;
  private String orderId;
  private String resourceId;
}

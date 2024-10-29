package com.consoleconnect.vortex.iam.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {
  private String userId;
  private String orgId;
  private boolean mgmt;
  private String customerId;
}

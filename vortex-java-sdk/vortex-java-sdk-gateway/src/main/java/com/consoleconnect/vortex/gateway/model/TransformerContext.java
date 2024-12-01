package com.consoleconnect.vortex.gateway.model;

import com.consoleconnect.vortex.iam.enums.UserTypeEnum;
import lombok.Data;

@Data
public class TransformerContext<T> {
  private String customerId;
  private UserTypeEnum loginUserType;
  private TransformerSpecification<T> specification;
}

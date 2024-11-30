package com.consoleconnect.vortex.gateway.model;

import lombok.Data;

@Data
public class TransformerContext<T> {
  private String customerId;
  private boolean mgmt;
  private TransformerSpecification<T> specification;
}

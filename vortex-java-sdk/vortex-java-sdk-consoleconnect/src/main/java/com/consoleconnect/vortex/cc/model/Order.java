package com.consoleconnect.vortex.cc.model;

import lombok.Data;

@Data
public class Order {
  private String id;
  private String portName;
  private String createdPortId;
  private String status;
}

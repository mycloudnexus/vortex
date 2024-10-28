package com.consoleconnect.vortex.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "white_label_order",
    indexes = {@Index(columnList = "order_id"), @Index(columnList = "organization_id")})
public class OrderEntity extends AbstractEntity {

  @Column(name = "organization_id", nullable = false)
  private String organizationId;

  @Column(name = "order_id", nullable = false, length = 100)
  private String orderId;

  @Column(name = "port_id", nullable = false, length = 100)
  private String portId;
}

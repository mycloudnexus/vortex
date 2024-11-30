package com.consoleconnect.vortex.gateway.entity;

import com.consoleconnect.vortex.core.entity.AbstractEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceStateEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "vortex_resource",
    indexes = {
      @Index(
          name = "vortex_resource_idx_orderId",
          columnList = "customer_id,resource_type,order_id"),
      @Index(
          name = "vortex_resource_idx_resourceId",
          columnList = "customer_id,resource_type,resource_id")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "vortex_resource_uni_orderId",
          columnNames = {"customer_id", "resource_type", "order_id"}),
      @UniqueConstraint(
          name = "vortex_resource_uni_resourceId",
          columnNames = {"customer_id", "resource_type", "resource_id"})
    })
public class ResourceEntity extends AbstractEntity {

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "order_id")
  private String orderId;

  @Column(name = "resource_id")
  private String resourceId;

  @Column(name = "resource_type", nullable = false)
  private String resourceType;

  @Enumerated(EnumType.STRING)
  @Column(name = "resource_state")
  private ResourceStateEnum resourceState;
}

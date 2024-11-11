package com.consoleconnect.vortex.gateway.entity;

import com.consoleconnect.vortex.core.entity.AbstractEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "vortex_order",
    indexes = {
      @Index(columnList = "organization_id,resource_type,order_id", unique = true),
      @Index(columnList = "organization_id,resource_type,resource_id", unique = true)
    })
public class OrderEntity extends AbstractEntity {

  @Column(name = "organization_id", nullable = false)
  private String organizationId;

  @Column(name = "order_id", length = 100)
  private String orderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "resource_type", length = 10)
  private ResourceTypeEnum resourceType;

  @Column(name = "resource_id", length = 100)
  private String resourceId;
}

package com.consoleconnect.vortex.gateway.entity;

import com.consoleconnect.vortex.core.entity.AbstractEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceStateEnum;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(
    name = "vortex_resource",
    indexes = {
      @Index(
          name = "vortex_resource_idx_orderId",
          columnList = "organization_id,resource_type,order_id"),
      @Index(
          name = "vortex_resource_idx_resourceId",
          columnList = "organization_id,resource_type,resource_id")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "vortex_resource_uni_orderId",
          columnNames = {"organization_id", "resource_type", "order_id"}),
      @UniqueConstraint(
          name = "vortex_resource_uni_resourceId",
          columnNames = {"organization_id", "resource_type", "resource_id"})
    })
public class ResourceEntity extends AbstractEntity {

  @Column(name = "organization_id", nullable = false)
  private String organizationId;

  @Column(name = "order_id")
  private String orderId;

  @Column(name = "resource_id")
  private String resourceId;

  @Enumerated(EnumType.STRING)
  @Column(name = "resource_type", nullable = false)
  private ResourceTypeEnum resourceType;

  @Enumerated(EnumType.STRING)
  @Column(name = "resource_state")
  private ResourceStateEnum resourceState;

  @Column(
      name = "sync_resource_config",
      nullable = true,
      unique = false,
      columnDefinition = "jsonb")
  @Type(JsonType.class)
  private SyncResourceConfig syncResourceConfig;

  @Data
  public static class SyncResourceConfig {
    private String method;
    private String path;
  }
}

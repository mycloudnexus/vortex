package com.consoleconnect.vortex.gateway.entity;

import com.consoleconnect.vortex.core.entity.AbstractEntity;
import com.consoleconnect.vortex.gateway.enums.AccessActionEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "vortex_path_access_rule",
    indexes = {
      @Index(name = "vortex_path_access_rule_idx_path", columnList = "path,method"),
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "vortex_path_access_rule_uni_path",
          columnNames = {"path", "method"})
    })
public class PathAccessRuleEntity extends AbstractEntity {

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "method", nullable = false)
  private String method;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false)
  private AccessActionEnum action;
}

package com.consoleconnect.vortex.iam.entity;

import com.consoleconnect.vortex.core.entity.AbstractEntity;
import com.consoleconnect.vortex.iam.enums.LoginTypeEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.enums.OrgTypeEnum;
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
    name = "vortex_organization",
    indexes = {
      @Index(columnList = "display_name", unique = true),
      @Index(columnList = "name", unique = true)
    })
public class OrganizationEntity extends AbstractEntity {

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private OrgStatusEnum status;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private OrgTypeEnum type;

  @Enumerated(EnumType.STRING)
  @Column(name = "login_type")
  private LoginTypeEnum loginType;
}

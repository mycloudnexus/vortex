package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.LoginTypeEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.enums.OrgTypeEnum;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationDto {

  private UUID id;

  private String name;

  private String displayName;

  private OrgStatusEnum status;

  private OrgTypeEnum type;

  private LoginTypeEnum loginType;

  private ZonedDateTime createdAt;

  private ZonedDateTime updatedAt;
}

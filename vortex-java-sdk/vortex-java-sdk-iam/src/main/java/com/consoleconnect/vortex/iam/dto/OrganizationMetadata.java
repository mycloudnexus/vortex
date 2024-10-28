package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.LoginTypeEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.enums.OrgTypeEnum;
import lombok.Data;

@Data
public class OrganizationMetadata {
  private OrgStatusEnum status = OrgStatusEnum.ACTIVE;
  private OrgTypeEnum type = OrgTypeEnum.CUSTOMER;
  private LoginTypeEnum loginType = LoginTypeEnum.UNDEFINED;
}

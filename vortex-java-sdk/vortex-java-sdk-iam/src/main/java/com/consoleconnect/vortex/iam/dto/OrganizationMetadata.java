package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.enums.OrgTypeEnum;
import lombok.Data;

@Data
public class OrganizationMetadata {
  private OrgStatusEnum status = OrgStatusEnum.ACTIVE;
  private OrgTypeEnum type = OrgTypeEnum.CUSTOMER;
  private ConnectionStrategyEnum loginType = ConnectionStrategyEnum.UNDEFINED;
}

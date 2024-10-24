package com.consoleconnect.vortex.core.model.req;

import com.consoleconnect.vortex.core.enums.CompanyStatusEnum;
import lombok.Data;

@Data
public class CompanyDto {

  private String companyName;

  // can not be update
  private String shortName;

  private CompanyStatusEnum status;
}

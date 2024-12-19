package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class DashboardOrganizationInfo {

  @JsonProperty("id")
  private String id;

  @JsonProperty("display_name")
  private String displayName;

  private OrgStatusEnum status;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
  private ZonedDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
  private ZonedDateTime lastBillingDate;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
  private ZonedDateTime nextBillingDate;
}

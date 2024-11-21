package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.UserStatusEnum;
import java.util.List;
import lombok.Data;

@Data
public class UpdateUserDto {

  private List<String> roles;

  private UserStatusEnum status;
}

package com.consoleconnect.vortex.iam.model;

import com.consoleconnect.vortex.iam.enums.UserStatus;
import java.util.Date;
import lombok.Data;

@Data
public class UserResponse {
  private String userId;
  private String email;
  private String firstName;
  private String lastName;
  private UserStatus userStatus;
  private String invitationURL;
  private Date createdAt;
  private Date expiresAt;
}

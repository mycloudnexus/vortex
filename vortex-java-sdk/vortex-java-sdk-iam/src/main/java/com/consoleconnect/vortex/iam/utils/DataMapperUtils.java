package com.consoleconnect.vortex.iam.utils;

import com.consoleconnect.vortex.iam.enums.UserStatus;
import java.util.Date;

public final class DataMapperUtils {
  public static UserStatus invitationStatus(Date expireAt) {
    UserStatus status = UserStatus.PENDING;
    if (expireAt.after(new Date())) {
      status = UserStatus.EXPIRED;
    }
    return status;
  }
}

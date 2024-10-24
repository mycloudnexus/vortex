package com.consoleconnect.vortex.iam.utils;

import com.consoleconnect.vortex.iam.enums.UserStatus;
import java.util.Date;
import org.apache.commons.lang3.BooleanUtils;

public final class DataMapperUtils {
  private DataMapperUtils() {}

  public static UserStatus invitationStatus(Date expireAt) {
    UserStatus status = UserStatus.PENDING;
    if (expireAt.before(new Date())) {
      status = UserStatus.EXPIRED;
    }
    return status;
  }

  public static UserStatus userStatus(Boolean blocked) {
    UserStatus status = UserStatus.ACTIVE;
    if (BooleanUtils.isTrue(blocked)) {
      status = UserStatus.DEACTIVATED;
    }
    return status;
  }
}

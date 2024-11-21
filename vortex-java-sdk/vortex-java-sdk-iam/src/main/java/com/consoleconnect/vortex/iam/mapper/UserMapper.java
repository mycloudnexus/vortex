package com.consoleconnect.vortex.iam.mapper;

import com.consoleconnect.vortex.iam.dto.User;
import com.consoleconnect.vortex.iam.entity.UserEntity;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserMapper {
  private UserMapper() {}

  public static final UserMapper INSTANCE = new UserMapper();

  public User toUser(UserEntity userEntity) {
    if (userEntity == null) {
      return null;
    }

    User user = new User();

    user.setId(userEntity.getUserId());
    if (userEntity.getCreatedAt() != null) {
      user.setCreatedAt(DateTimeFormatter.ISO_DATE_TIME.format(userEntity.getCreatedAt()));
    }
    user.setCreatedBy(userEntity.getCreatedBy());
    if (userEntity.getUpdatedAt() != null) {
      user.setUpdatedAt(DateTimeFormatter.ISO_DATE_TIME.format(userEntity.getUpdatedAt()));
    }
    user.setUpdatedBy(userEntity.getUpdatedBy());
    List<String> list = userEntity.getRoles();
    if (list != null) {
      user.setRoles(new ArrayList<>(list));
    }
    user.setStatus(userEntity.getStatus());

    return user;
  }
}

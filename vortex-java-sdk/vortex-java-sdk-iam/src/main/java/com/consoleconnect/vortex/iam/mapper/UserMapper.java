package com.consoleconnect.vortex.iam.mapper;

import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.iam.dto.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserInfo toUserInfo(User user);
}

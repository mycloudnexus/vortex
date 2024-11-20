package com.consoleconnect.vortex.iam.mapper;

import com.consoleconnect.vortex.iam.dto.MemberInfo;
import com.consoleconnect.vortex.iam.dto.User;
import com.consoleconnect.vortex.iam.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  MemberInfo toMemberInfo(com.auth0.json.mgmt.users.User user);

  @Mapping(target = "id", source = "userId")
  User toUser(UserEntity userEntity);
}

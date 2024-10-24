package com.consoleconnect.vortex.iam.utils;

import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.iam.model.UserResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DataMapper {
  DataMapper INSTANCE = Mappers.getMapper(DataMapper.class);

  List<UserResponse> memberToUserResponses(List<User> userList);

  @Mapping(source = "familyName", target = "lastName")
  @Mapping(source = "givenName", target = "firstName")
  @Mapping(target = "userStatus", constant = "ACTIVE")
  UserResponse memberToUserResponse(User user);

  List<UserResponse> invitationToUserResponses(List<Invitation> userList);

  @Mapping(source = "invitee.email", target = "email")
  @Mapping(
      target = "userStatus",
      expression = "java(DataMapperUtils.invitationStatus(user.getExpiresAt()))")
  UserResponse invitationToUserResponse(Invitation user);
}

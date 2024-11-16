package com.consoleconnect.vortex.iam.service;

import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.organizations.Invitation;
import com.auth0.json.mgmt.organizations.Organization;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.consoleconnect.vortex.cc.CCHttpClient;
import com.consoleconnect.vortex.cc.model.UserInfo;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.DateTime;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.iam.auth0.Auth0Client;
import com.consoleconnect.vortex.iam.dto.*;
import com.consoleconnect.vortex.iam.entity.UserEntity;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.enums.UserStatusEnum;
import com.consoleconnect.vortex.iam.mapper.UserMapper;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class UserService {
  private final Auth0Client auth0Client;
  private final UserRepository userRepository;
  private final IamProperty iamProperty;
  private final EmailService emailService;
  private final CCHttpClient downstreamRoleService;

  @Transactional
  @PostConstruct
  public void initialize() {
    log.info("UserService initialized");

    if (userRepository.count() > 0) {
      log.info("UserRepository already initialized");
      return;
    }

    if (iamProperty.getPlatformAdmins() != null) {
      for (String userId : iamProperty.getPlatformAdmins()) {
        log.info("platform Admin: {}", userId);
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userId);
        userEntity.setStatus(UserStatusEnum.ACTIVE);
        userEntity.setRoles(List.of(RoleEnum.PLATFORM_ADMIN.toString()));
        userRepository.save(userEntity);
      }
    }
  }

  public UserEntity create(CreateUserDto dto, String createdBy) {
    log.info("Creating user: {},createdBy:{}", dto, createdBy);
    // check if the user already in the organization
    // list all members of the organization
    // find userId based on the email
    String userId = "";
    // check if the userId has been in the organization
    UserEntity userEntity = userRepository.findOneByUserId(userId).orElseGet(UserEntity::new);
    userEntity.setStatus(UserStatusEnum.ACTIVE);
    userEntity.setRoles(dto.getRoles());
    userEntity.setCreatedBy(createdBy);

    userEntity = userRepository.save(userEntity);
    if (dto.isSendEmail()) {
      // send email
      Invitation invitation = new Invitation(null, null, null);
      emailService.sendInvitation(invitation);
    }
    return userEntity;
  }

  public UserEntity update(String userId, UpdateUserDto request, String updatedBy) {
    log.info("Updating user: {},request:{},updatedBy:{}", userId, request, updatedBy);
    UserEntity userEntity =
        userRepository
            .findOneByUserId(userId)
            .orElseThrow(() -> VortexException.notFound("User not found"));
    if (request.getStatus() != null) userEntity.setStatus(request.getStatus());
    if (request.getRoles() != null) userEntity.setRoles(request.getRoles());
    userEntity.setUpdatedBy(updatedBy);
    return userRepository.save(userEntity);
  }

  public UserEntity delete(String userId, String deletedBy) {
    log.info("Deleting user: {},deletedBy:{}", userId, deletedBy);
    UserEntity userEntity =
        userRepository
            .findOneByUserId(userId)
            .orElseThrow(() -> VortexException.notFound("User not found"));
    if (userEntity.getStatus() == UserStatusEnum.DELETED) {
      throw VortexException.badRequest("User already deleted");
    }
    userEntity.setStatus(UserStatusEnum.DELETED);
    userEntity.setDeletedBy(deletedBy);
    userEntity.setDeletedAt(DateTime.nowInUTC());
    return userRepository.save(userEntity);
  }

  public com.consoleconnect.vortex.iam.dto.UserInfo getInfo(String userId) {
    try {
      UsersEntity userEntity = auth0Client.getMgmtClient().users();
      User user = userEntity.get(userId, null).execute().getBody();
      List<Organization> organizations =
          userEntity.getOrganizations(userId, null).execute().getBody().getItems();
      com.consoleconnect.vortex.iam.dto.UserInfo userInfo = UserMapper.INSTANCE.toUserInfo(user);
      if (organizations != null && !organizations.isEmpty()) {
        if (organizations.size() > 1) {
          log.warn("User {} belongs to multiple organizations", userId);
        }
        Organization org = organizations.get(0);
        String orgId = org.getId();
        List<Role> roles =
            auth0Client
                .getMgmtClient()
                .organizations()
                .getRoles(orgId, userId, null)
                .execute()
                .getBody()
                .getItems();

        com.consoleconnect.vortex.iam.dto.UserInfo.UserOrganization userOrganization =
            JsonToolkit.fromJson(
                JsonToolkit.toJson(org),
                com.consoleconnect.vortex.iam.dto.UserInfo.UserOrganization.class);

        userOrganization.setRoles(roles);
        userInfo.setOrganization(userOrganization);
      } else {
        log.warn("User {} does not belong to any organization", userId);
      }
      return userInfo;
    } catch (Auth0Exception ex) {
      log.error("Failed to get user info", ex);
      throw VortexException.internalError("Failed to get user info");
    }
  }

  public Paging<User> searchUsers(int page, int size) {
    return null;
  }

  public UserInfo downstreamUserInfo(String userId, Jwt jwt) {
    try {

      UsersEntity userEntity = auth0Client.getMgmtClient().users();
      User user = userEntity.get(userId, null).execute().getBody();
      List<Organization> organizations =
          userEntity.getOrganizations(userId, null).execute().getBody().getItems();
      Organization organization = organizations.get(0);
      List<String> resourceRoles =
          jwt.getClaimAsStringList(iamProperty.getJwt().getCustomClaims().getRoles());
      log.info(
          "downstream userinfo, user.email:{} orgId:{}", user.getEmail(), organization.getId());

      boolean mgmt = false;
      if (auth0Client.getAuth0Property().getMgmtOrgId().equalsIgnoreCase(organization.getId())
          && (resourceRoles.contains(RoleEnum.PLATFORM_ADMIN.name())
              || resourceRoles.contains(RoleEnum.PLATFORM_MEMBER.name()))) {
        mgmt = true;
      }

      return downstreamRoleService.getUserInfo(user.getEmail(), mgmt);
    } catch (Exception e) {
      log.error("downstream userinfo error", e);
      throw VortexException.badRequest("Retrieve downstream userinfo error.");
    }
  }
}

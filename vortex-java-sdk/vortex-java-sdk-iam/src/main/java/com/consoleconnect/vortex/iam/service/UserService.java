package com.consoleconnect.vortex.iam.service;

import com.auth0.json.mgmt.organizations.Invitation;
import com.consoleconnect.vortex.cc.ConsoleConnectClient;
import com.consoleconnect.vortex.cc.ConsoleConnectClientFactory;
import com.consoleconnect.vortex.cc.model.Member;
import com.consoleconnect.vortex.cc.model.UserInfo;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.DateTime;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.iam.dto.CreateUserDto;
import com.consoleconnect.vortex.iam.dto.UpdateUserDto;
import com.consoleconnect.vortex.iam.dto.User;
import com.consoleconnect.vortex.iam.entity.UserEntity;
import com.consoleconnect.vortex.iam.enums.RoleEnum;
import com.consoleconnect.vortex.iam.enums.UserStatusEnum;
import com.consoleconnect.vortex.iam.mapper.UserMapper;
import com.consoleconnect.vortex.iam.model.IamProperty;
import com.consoleconnect.vortex.iam.model.UserContext;
import com.consoleconnect.vortex.iam.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final IamProperty iamProperty;
  private final EmailService emailService;
  private final UserContextService userContextService;

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

  public ConsoleConnectClient createClient(String bearerToken) {
    return ConsoleConnectClientFactory.create(
        iamProperty.getDownStream().getBaseUrl(), bearerToken);
  }

  private Member getMemberById(UserContext userContext, String userId) {
    ConsoleConnectClient consoleConnectClient = userContext.getConsoleConnectClient();
    return consoleConnectClient.listMembers(userContext.getOrgId()).stream()
        .filter(member -> member.getId().equals(userId))
        .findFirst()
        .orElseThrow(() -> VortexException.notFound("Member not found"));
  }

  private UserInfo getUserInfo(UserContext userContext, String username) {
    ConsoleConnectClient consoleConnectClient = userContext.getConsoleConnectClient();
    return consoleConnectClient.getUserByUsername(username);
  }

  public User create(CreateUserDto dto, JwtAuthenticationToken token) {
    UserContext userContext = userContextService.createUserContext(token, true);
    log.info("Creating user: {},createdBy:{}", dto, userContext.getUserId());

    Member member = getMemberById(userContext, dto.getUserId());

    UserEntity userEntity =
        userRepository.findOneByUserId(dto.getUserId()).orElseGet(UserEntity::new);
    userEntity.setStatus(UserStatusEnum.ACTIVE);
    if (dto.getRoles() == null) {
      dto.setRoles(userContext.getTrustedIssuer().getDefaultRoles());
    }
    userEntity.setUserId(dto.getUserId());
    userEntity.setRoles(dto.getRoles());
    userEntity.setCreatedBy(userContext.getUserId());

    userEntity = userRepository.save(userEntity);
    if (dto.isSendEmail()) {
      // send email
      Invitation invitation = new Invitation(null, null, null);
      emailService.sendInvitation(invitation);
    }
    return toUser(userEntity, member, userContext.getOrgId());
  }

  public User update(String userId, UpdateUserDto request, JwtAuthenticationToken token) {
    UserContext userContext = userContextService.createUserContext(token, true);
    log.info("Updating user: {},request:{},updatedBy:{}", userId, request, userContext.getUserId());
    UserEntity userEntity =
        userRepository
            .findOneByUserId(userId)
            .orElseThrow(() -> VortexException.notFound("User not found"));
    if (request.getStatus() != null) userEntity.setStatus(request.getStatus());
    if (request.getRoles() != null) userEntity.setRoles(request.getRoles());
    userEntity.setUpdatedBy(userContext.getUserId());
    return toUser(userRepository.save(userEntity), null, userContext.getOrgId());
  }

  public User delete(String userId, JwtAuthenticationToken token) {
    UserContext userContext = userContextService.createUserContext(token, true);
    String deletedBy = userContext.getUserId();
    log.info("Deleting user: {},deletedBy:{}", userId, deletedBy);
    if (userId.equals(deletedBy)) {
      throw VortexException.badRequest("Cannot delete self");
    }
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
    return toUser(userRepository.save(userEntity), null, userContext.getOrgId());
  }

  public User getUserInfo(JwtAuthenticationToken token) {
    return getUserInfo(null, token);
  }

  public User getUserInfo(String userId, JwtAuthenticationToken token) {
    UserContext userContext = userContextService.createUserContext(token, true);
    if (userId == null) {
      userId = userContext.getUserId();
    }
    log.info("Getting user info,userId:{},searchBy:{}", userId, userContext.getUserId());
    UserEntity userEntity =
        userRepository
            .findOneByUserId(userId)
            .orElseThrow(() -> VortexException.notFound("User not found"));
    Member member = getMemberById(userContext, userId);
    User user = toUser(userEntity, member, userContext.getOrgId());

    UserInfo userInfo = getUserInfo(userContext, member.getUsername());

    if (userInfo.getLinkUserCompany() != null
        && userInfo.getLinkUserCompany().containsKey(user.getOrganizationId())) {
      user.setOrganization(userInfo.getLinkUserCompany().get(user.getOrganizationId()));
    } else {
      log.warn("User {} does not belong to organization({})", userId, user.getOrganizationId());
    }

    return user;
  }

  public Paging<User> search(int page, int size, JwtAuthenticationToken token) {

    final UserContext userContext = userContextService.createUserContext(token, true);
    log.info("Searching users,page:{},size:{},searchBy:{}", page, size, userContext.getUserId());

    final Map<String, Member> id2Member =
        userContext.getConsoleConnectClient().listMembers(userContext.getOrgId()).stream()
            .collect(Collectors.toMap(Member::getId, m -> m));

    Page<UserEntity> userEntities = userRepository.search(PagingHelper.toPageable(page, size));

    return PagingHelper.toPaging(
        userEntities,
        userEntity ->
            toUser(userEntity, id2Member.get(userEntity.getUserId()), userContext.getOrgId()));
  }

  private User toUser(UserEntity userEntity, Member member, String orgId) {
    User user = UserMapper.INSTANCE.toUser(userEntity);
    user.setOrganizationId(orgId);

    if (member != null) {
      user.setName(member.getName());
      user.setEmail(member.getEmail());
      user.setUsername(member.getUsername());
    }

    return user;
  }
}

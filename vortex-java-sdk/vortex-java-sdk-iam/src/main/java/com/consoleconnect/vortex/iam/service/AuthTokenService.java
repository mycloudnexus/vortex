package com.consoleconnect.vortex.iam.service;

import com.consoleconnect.vortex.iam.dto.AuthToken;
import com.consoleconnect.vortex.iam.dto.MemberInfo;
import com.consoleconnect.vortex.iam.dto.User;
import com.consoleconnect.vortex.iam.enums.UserTypeEnum;
import com.consoleconnect.vortex.iam.model.UserContext;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthTokenService {
  private final UserService userService;
  private final MemberService memberService;
  private final UserContextService userContextService;

  public AuthToken getAuthToken(JwtAuthenticationToken jwt) {
    UserContext userContext = userContextService.createUserContext(jwt);

    AuthToken authToken = new AuthToken();
    authToken.setUserId(userContext.getUserId());
    authToken.setOrgId(userContext.getOrgId());
    authToken.setUserType(userContext.getUserType());
    authToken.setRoles(userContext.getRoles());

    if (userContext.getUserType() == UserTypeEnum.MGMT_USER) {
      User user = userService.getUserInfo(jwt);

      authToken.setName(user.getName());
      authToken.setEmail(user.getEmail());

      authToken.setUserInfo(user);
    } else {
      MemberInfo memberInfo = memberService.getUserInfo(authToken.getUserId());
      authToken.setName(memberInfo.getName());
      authToken.setEmail(memberInfo.getEmail());
      authToken.setUserInfo(memberInfo);
    }
    return authToken;
  }
}

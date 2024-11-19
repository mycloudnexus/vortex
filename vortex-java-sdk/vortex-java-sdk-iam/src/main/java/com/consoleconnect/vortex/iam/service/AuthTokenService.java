package com.consoleconnect.vortex.iam.service;

import com.consoleconnect.vortex.iam.dto.AuthToken;
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
    authToken.setMgmt(userContext.isMgmt());
    authToken.setRoles(jwt.getAuthorities().stream().map(Object::toString).toList());

    if (userContext.isMgmt()) {
      authToken.setUserInfo(memberService.getUserInfo(authToken.getUserId()));
    } else {
      authToken.setUserInfo(userService.getUserInfo(jwt));
    }
    return authToken;
  }
}

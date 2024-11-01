package com.consoleconnect.vortex.gateway.toolkit;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.model.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.server.ServerWebExchange;

public class RequestHelper {

  private RequestHelper() {}

  public static UserContext getUserContext(ServerWebExchange exchange) {
    UserContext userContext = exchange.getAttribute(IamConstants.X_VORTEX_USER_CONTEXT);
    if (userContext == null) {
      throw VortexException.badRequest("User information not found.");
    }
    return userContext;
  }

  public static String getCurrentOrgId(ServerWebExchange exchange) {
    UserContext userContext = RequestHelper.getUserContext(exchange);

    if (StringUtils.isNotBlank(userContext.getCustomerId())) {
      return userContext.getCustomerId();
    }

    throw VortexException.badRequest("Invalid organization id");
  }
}

package com.consoleconnect.vortex.iam.service; // package com.consoleconnect.vortex.cc;

import com.consoleconnect.vortex.cc.CCHttpClient;
import com.consoleconnect.vortex.iam.model.IamProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Data
@Service
@AllArgsConstructor
public class DownstreamRoleService {
  private final CCHttpClient ccHttpClient;
  private final IamProperty iamProperty;

  @Async
  public void syncRole(String orgId, String username) {
    log.info("syncRole, orgId:{}, username:{}", orgId, username);

    ccHttpClient.assignRole2Member(username, iamProperty.getDownStream().getRole());
  }
}

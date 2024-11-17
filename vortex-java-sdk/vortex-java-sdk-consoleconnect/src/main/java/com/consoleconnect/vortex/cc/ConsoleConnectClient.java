package com.consoleconnect.vortex.cc;

import com.consoleconnect.vortex.cc.model.*;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "consoleconnect")
public interface ConsoleConnectClient {
  @GetMapping(value = "/heartbeat")
  Heartbeat getHeartbeat();

  @GetMapping(value = "/api/auth/token")
  UserInfo getCurrentUser();

  @GetMapping(value = "/api/user/{username}")
  UserInfo getUserByUsername(@PathVariable("username") String username);

  @GetMapping(value = "/v2/companies/{companyId}/members?pageSize=0")
  List<Member> listMembers(@PathVariable("companyId") String companyId);

  @GetMapping(value = "/api/company/{companyUsername}/ports/orders")
  Results<Order> listOrders(@PathVariable("companyUsername") String companyUsername);
}

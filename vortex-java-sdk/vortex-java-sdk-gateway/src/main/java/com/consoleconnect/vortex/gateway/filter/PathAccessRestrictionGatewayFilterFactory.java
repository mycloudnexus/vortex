package com.consoleconnect.vortex.gateway.filter;

import com.consoleconnect.vortex.gateway.dto.PathAccessRule;
import com.consoleconnect.vortex.gateway.enums.AccessActionEnum;
import com.consoleconnect.vortex.gateway.model.GatewayProperty;
import com.consoleconnect.vortex.gateway.service.PathAccessRuleService;
import com.consoleconnect.vortex.iam.enums.CustomerTypeEnum;
import com.consoleconnect.vortex.iam.model.IamConstants;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PathAccessRestrictionGatewayFilterFactory
    extends AbstractGatewayFilterFactory<PathAccessRestrictionGatewayFilterFactory.Config> {

  private final PathAccessRuleService pathAccessRuleService;
  private final GatewayProperty gatewayProperty;

  public PathAccessRestrictionGatewayFilterFactory(
      PathAccessRuleService pathAccessRuleService, GatewayProperty gatewayProperty) {
    super(Config.class);
    this.pathAccessRuleService = pathAccessRuleService;
    this.gatewayProperty = gatewayProperty;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new PathAccessRuleFilter(config, pathAccessRuleService, gatewayProperty);
  }

  public static class PathAccessRuleFilter implements GatewayFilter, Ordered {

    private final PathAccessRestrictionGatewayFilterFactory.Config config;
    private final PathAccessRuleService pathAccessRuleService;
    private final GatewayProperty gatewayProperty;

    public PathAccessRuleFilter(
        PathAccessRestrictionGatewayFilterFactory.Config config,
        PathAccessRuleService pathAccessRuleService,
        GatewayProperty gatewayProperty) {
      this.config = config;
      this.pathAccessRuleService = pathAccessRuleService;
      this.gatewayProperty = gatewayProperty;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
      CustomerTypeEnum customerType = exchange.getAttribute(IamConstants.X_VORTEX_CUSTOMER_TYPE);
      log.info("PathAccessRuleFilter, customerType:{}", customerType);

      if (customerType != config.getCustomerType()) {
        return chain.filter(exchange);
      }

      String method = exchange.getRequest().getMethod().name();
      String path = exchange.getRequest().getURI().getPath();
      if (path.startsWith(gatewayProperty.getPathPrefix())) {
        path = path.substring(gatewayProperty.getPathPrefix().length());
      }
      log.info("making access decision on ,{} {}", method, path);
      List<PathAccessRule> rules = new ArrayList<>();
      rules.addAll(
          config.getAllowed().stream()
              .flatMap(p -> p.toPathAccessRules(AccessActionEnum.ALLOWED).stream())
              .toList());
      rules.addAll(
          config.getDenied().stream()
              .flatMap(p -> p.toPathAccessRules(AccessActionEnum.DENIED).stream())
              .toList());
      AccessActionEnum decision = pathAccessRuleService.makeAccessDecision(method, path, rules);
      if (decision == AccessActionEnum.UNDEFINED) {
        decision = config.getDefaultAction();
      }
      log.info("Access decision is {}", decision);
      if (decision != AccessActionEnum.ALLOWED) {
        // Return 403 Forbidden
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
      }
      return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
      return NettyWriteResponseFilter.HIGHEST_PRECEDENCE;
    }
  }

  @Data
  public static class Config {
    private CustomerTypeEnum customerType = CustomerTypeEnum.CUSTOMER;
    private AccessActionEnum defaultAction = AccessActionEnum.ALLOWED;

    private List<PathDefinition> allowed = List.of();
    private List<PathDefinition> denied = List.of();
  }

  @Data
  public static class PathDefinition {
    private String path;
    private List<String> methods;

    public List<PathAccessRule> toPathAccessRules(AccessActionEnum action) {
      return methods.stream()
          .map(
              method -> {
                PathAccessRule rule = new PathAccessRule();
                rule.setMethod(method);
                rule.setPath(path);
                rule.setAction(action);
                return rule;
              })
          .toList();
    }
  }
}

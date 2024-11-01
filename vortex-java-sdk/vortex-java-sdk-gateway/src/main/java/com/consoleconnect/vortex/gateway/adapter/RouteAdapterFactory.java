package com.consoleconnect.vortex.gateway.adapter;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.gateway.config.RouteAdapterConfig;
import com.consoleconnect.vortex.gateway.config.RouteAdapterProperty;
import com.consoleconnect.vortex.gateway.service.OrderService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Component
public class RouteAdapterFactory {

  private Map<String, RouteAdapter> adapterMap = new HashMap<>();

  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  public RouteAdapterFactory(RouteAdapterConfig config, OrderService orderService) {

    RouteAdapterContext context = new RouteAdapterContext(orderService);

    for (RouteAdapterProperty route : config.getRouteAdapters()) {
      HttpMethod method = route.getMethod();
      String routePath = route.getRoutePath();
      String adapterClassName = route.getAdapterClassName();

      if (method == null || routePath == null || adapterClassName == null) {
        throw VortexException.notImplemented("Route adapter config error");
      }

      try {
        Class<?> adapterClass = Class.forName(adapterClassName);
        RouteAdapter adapter =
            (RouteAdapter)
                adapterClass.getDeclaredConstructor(RouteAdapterContext.class).newInstance(context);

        // register adapter
        adapterMap.put(buildAdapterKey(method, routePath), adapter);
      } catch (Exception e) {
        log.error("Register route adapter class:{} failed.", adapterClassName, e);
        throw VortexException.notImplemented(
            "Register route adapter class failed: " + adapterClassName);
      }
    }
  }

  public RouteAdapter matchAdapter(ServerWebExchange exchange) {
    String key =
        buildAdapterKey(
            exchange.getRequest().getMethod(), exchange.getRequest().getURI().getPath());

    for (Map.Entry<String, RouteAdapter> entry : adapterMap.entrySet()) {
      if (pathMatcher.match(entry.getKey(), key)) {
        return entry.getValue();
      }
    }
    return null;
  }

  private String buildAdapterKey(HttpMethod method, String routePath) {
    return method.name() + " " + routePath;
  }
}

package com.consoleconnect.vortex.gateway.transformer;

import static com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum.DEFAULT_RESOURCE_LIST;

import com.consoleconnect.vortex.gateway.entity.OrderEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.service.OrderService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultResourceListTransformer extends AbstractResourceTransformer<Object> {

  protected final OrderService orderService;

  public DefaultResourceListTransformer(OrderService orderService) {
    super(Object.class);
    this.orderService = orderService;
  }

  /**
   * process result and error code
   *
   * @param responseBody
   * @return
   */
  @Override
  public String doTransform(String responseBody, TransformerContext<Object> context) {

    // filter resource by organization
    Map<String, OrderEntity> resources =
        orderService
            .listResourceByType(
                context.getCustomerId(),
                ResourceTypeEnum.valueOf(context.getSpecification().getResourceType()))
            .stream()
            .collect(Collectors.toMap(OrderEntity::getResourceId, x -> x));

    Set<String> resourceIds = resources.keySet();

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    List<Map<String, Object>> resOrders =
        ctx.read(context.getSpecification().getResponseDataPath());

    // filter resources
    resOrders.removeIf(o -> filterResource(resourceIds, o, context.getSpecification()));

    if (TransformerSpecification.JSON_ROOT.equals(
        context.getSpecification().getResponseDataPath())) {
      // override ctx
      ctx = JsonPathToolkit.createDocCtx(resOrders);
    } else {
      ctx.set(context.getSpecification().getResponseDataPath(), resOrders);
    }

    log.info("process completed, resourceType:{}", context.getSpecification().getResourceType());
    return ctx.jsonString();
  }

  // default filter
  protected boolean filterResource(
      Set<String> resourceIds, Map<String, Object> dto, TransformerSpecification<Object> config) {
    String oId = (String) dto.get(config.getResourceInstanceId());
    if (resourceIds.contains(oId)) {
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  @Override
  public TransformerIdentityEnum getTransformerId() {
    return DEFAULT_RESOURCE_LIST;
  }
}

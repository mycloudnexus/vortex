package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.Hook;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.SpelExpressionEngine;
import com.consoleconnect.vortex.iam.enums.CustomerTypeEnum;
import com.consoleconnect.vortex.iam.enums.UserTypeEnum;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Service
public class TransformerChain {

  public static final String VAR_RESOURCES = "resources";
  public static final String VAR_ORDER_IDS = "orderIds";
  public static final String VAR_RESOURCE_IDS = "resourceIds";
  public static final String VAR_CUSTOMER_ID = "customerId";
  public static final String VAR_USER_TYPE = "userType";
  public static final String VAR_USER_ID = "userId";
  public static final String VAR_CUSTOMER_TYPE = "customerType";

  protected final ResourceService resourceService;
  protected final OrganizationService organizationService;

  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  private final Map<TransformerIdentityEnum, AbstractTransformer<?>> chainMap;

  public TransformerChain(
      OrganizationService organizationService,
      ResourceService resourceService,
      List<AbstractTransformer> chains) {
    this.organizationService = organizationService;
    this.resourceService = resourceService;
    this.chainMap =
        chains.stream()
            .collect(Collectors.toMap(chain -> chain.getTransformerId(), chain -> chain));
  }

  public final byte[] transform(
      ServerWebExchange exchange, byte[] responseBody, TransformerSpecification specification) {
    log.info("Start to transform,specification:{}", specification);
    long start = System.currentTimeMillis();
    try {

      String userId = exchange.getAttribute(IamConstants.X_VORTEX_USER_ID);
      UserTypeEnum userType = exchange.getAttribute(IamConstants.X_VORTEX_USER_TYPE);
      String customerId = exchange.getAttribute(IamConstants.X_VORTEX_CUSTOMER_ID);
      CustomerTypeEnum customerType = exchange.getAttribute(IamConstants.X_VORTEX_CUSTOMER_TYPE);

      TransformerSpecification specificationInternal = specification.copy();
      TransformerContext context = new TransformerContext();
      context.setHttpMethod(exchange.getRequest().getMethod());
      context.setPath(exchange.getRequest().getURI().getPath());
      context.setCustomerId(customerId);
      context.setCustomerType(customerType);
      context.setSpecification(specificationInternal);
      context.setUserId(userId);
      context.setUserType(userType);
      context.setVariables(buildVariables(context));

      if (!canTransform(context)) {
        log.info("Skip transform, condition not met.");
        return responseBody;
      }

      String responseBodyJsonStr = new String(responseBody, StandardCharsets.UTF_8);
      // Run transformer chains
      for (TransformerSpecification.TransformerChain chain :
          context.getSpecification().getTransformerChains()) {
        responseBodyJsonStr =
            chainMap.get(chain.getChainName()).doTransform(responseBodyJsonStr, context, chain);
        afterTransform(context.getData(), context, chain); // update resource id
      }
      log.info("Transform done, time: {} ms.", System.currentTimeMillis() - start);
      return responseBodyJsonStr.getBytes(StandardCharsets.UTF_8);
    } catch (Exception e) {
      String errorMsg = "Failed to transform,error:{}" + e.getMessage();
      log.error("{}", errorMsg);
      throw VortexException.badRequest(errorMsg);
    }
  }

  public boolean canTransform(TransformerContext context) {
    log.info("Check if can transform,context:{}", context.getUserType());
    if (context.getSpecification().getWhen() == null
        || context.getSpecification().getWhen().isEmpty()) {
      log.info("No condition,transform directly.");
      return true;
    }
    log.info("when:{}", context.getSpecification().getWhen());
    Boolean conditionOn =
        SpelExpressionEngine.evaluate(
            context.getSpecification().getWhen(), context.getVariables(), Boolean.class);
    log.info("Condition on:{}", conditionOn);
    return conditionOn != null && conditionOn;
  }

  public Map<String, Object> buildVariables(TransformerContext context) {
    // filter resource by customerId and resourceType
    log.info(
        "build variables:customerId:{},resourceType:{}",
        context.getCustomerId(),
        context.getSpecification().getResourceType());
    List<ResourceEntity> resources =
        resourceService.findAllByCustomerIdAndResourceType(
            context.getCustomerId(), context.getSpecification().getResourceType());

    log.info("resources:{}", resources.size());
    List<String> orderIds =
        resources.stream().map(ResourceEntity::getOrderId).filter(Objects::nonNull).toList();

    List<String> resourceIds =
        resources.stream().map(ResourceEntity::getResourceId).filter(Objects::nonNull).toList();

    Map<String, Object> variables = new HashMap<>();
    variables.put(VAR_ORDER_IDS, orderIds);
    variables.put(VAR_RESOURCE_IDS, resourceIds);
    variables.put(VAR_RESOURCES, resources);
    variables.put(VAR_CUSTOMER_TYPE, context.getCustomerType().name());
    variables.put(VAR_CUSTOMER_ID, context.getCustomerId());
    variables.put(VAR_USER_TYPE, context.getUserType().name());
    variables.put(VAR_USER_ID, context.getUserId());

    variables.putAll(
        pathMatcher.extractUriTemplateVariables(
            context.getSpecification().getHttpPath(), context.getPath()));

    return variables;
  }

  @SuppressWarnings("unchecked")
  protected void afterTransform(
      List<Object> data,
      TransformerContext context,
      TransformerSpecification.TransformerChain chain) {
    log.info("afterTransform:{}", data);
    if (data == null || data.isEmpty()) {
      log.info("No data to transform,skip.");
      return;
    }

    if (chain.getAfterTransformHooks() == null || chain.getAfterTransformHooks().isEmpty()) {
      log.info("No afterTransform hooks,skip.");
      return;
    }

    runHooks(
        data,
        (List<ResourceEntity>) context.getVariables().get(VAR_RESOURCES),
        chain.getAfterTransformHooks());
  }

  private void runHooks(
      List<Object> data, List<ResourceEntity> resources, List<Hook.Default> hooks) {

    for (Hook.Default hook : hooks) {
      log.info("Hook:{}", hook);
      if ("SYNC_RESOURCE_ID".equalsIgnoreCase(hook.getId())) {
        syncResourceId(data, resources, hook);
      } else {
        log.info("Unknown hook:{}", hook.getId());
      }
    }
  }

  private void syncResourceId(
      List<Object> data, List<ResourceEntity> resources, Hook.Default hook) {
    log.info("Sync resource id.");
    for (Object obj : data) {
      String objStr = JsonToolkit.toJson(obj);

      String orderIdPath = String.format("$.%s", hook.getOptions().get("orderId"));
      String resourceIdPath = String.format("$.%s", hook.getOptions().get("resourceId"));

      String orderId = JsonPathToolkit.read(objStr, orderIdPath);
      String resourceId = JsonPathToolkit.read(objStr, resourceIdPath);

      log.info("orderId:{},resourceId:{}", orderId, resourceId);
      if (orderId != null && resourceId != null) {
        Optional<ResourceEntity> resourceEntityOptional =
            resources.stream().filter(r -> orderId.equals(r.getOrderId())).findFirst();
        if (resourceEntityOptional.isPresent()) {
          resourceEntityOptional.get().setResourceId(resourceId);
          log.info("Resource found,update resource id.");
        } else {
          log.info("Resource not found,create new one.");
        }
      }
    }
    resourceService.updateAll(resources);
  }
}

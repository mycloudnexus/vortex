package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.enums.TransformerIdentityEnum;
import com.consoleconnect.vortex.gateway.model.TransformerContext;
import com.consoleconnect.vortex.gateway.model.TransformerSpecification;
import com.consoleconnect.vortex.gateway.service.ResourceService;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.gateway.toolkit.SpelExpressionEngine;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ListModifyTransformer extends AbstractTransformer<ListModifyTransformer.Options> {

  private static final String JSON_ARRAY_PATH_FORMAT = "%s[%d].%s";

  protected ListModifyTransformer(
      OrganizationService organizationService, ResourceService resourceService) {
    super(organizationService, resourceService);
  }

  @Override
  public String doTransform(
      String responseBody,
      TransformerContext context,
      TransformerSpecification.TransformerChain<ListModifyTransformer.Options> chain) {

    Options options = chain.getChanOptions(Options.class);

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    // data to be filtered, it MUST be a list
    List<Object> data = ctx.read(context.getSpecification().getResponseDataPath());

    for (int i = 0; i < data.size(); i++) {

      fillValByKeyPath(context, options, i, ctx);

      if (options.getWhen() != null
          && Boolean.TRUE.equals(
              SpelExpressionEngine.evaluate(
                  options.getWhen(), context.getVariables(), Boolean.class))) {

        for (Map.Entry<String, Object> property : options.getModifier().entrySet()) {
          String path =
              String.format(
                  JSON_ARRAY_PATH_FORMAT,
                  context.getSpecification().getResponseDataPath(),
                  i,
                  property.getKey());
          Object updatingValue =
              SpelExpressionEngine.evaluate(
                  property.getValue(), context.getVariables(), Object.class);
          String mainJson = JsonToolkit.toJson(ctx.read(path));
          String updatingJson = JsonToolkit.toJson(updatingValue);
          String updatedJson =
              JsonToolkit.merge(mainJson, updatingJson)
                  .orElseThrow(() -> VortexException.internalError("Failed to merge json"));
          ctx.set(path, JsonToolkit.fromJson(updatedJson, Object.class));
        }
      }
    }

    return ctx.jsonString();
  }

  @Override
  public TransformerIdentityEnum getTransformerId() {
    return TransformerIdentityEnum.RESOURCES_LIST_AND_MODIFY;
  }

  private void fillValByKeyPath(
      TransformerContext context, Options options, int index, DocumentContext ctx) {

    for (Map.Entry<String, String> e : options.getKeysPath().entrySet()) {
      // reset path-value for each item keys

      String[] pathValArr = e.getValue().split(":");
      String keyPath = e.getValue();
      if (pathValArr.length == 2) {
        keyPath = pathValArr[0];
      }

      String path =
          String.format(
              JSON_ARRAY_PATH_FORMAT,
              context.getSpecification().getResponseDataPath(),
              index,
              keyPath);
      log.info("fillValByKeyPath path:{}", path);

      Object value = readWithNull(ctx, path);

      if (value == null && pathValArr.length == 2) {
        value = pathValArr[1]; // default value
      }

      context.getVariables().put(e.getKey(), value);
    }
  }

  private Object readWithNull(DocumentContext ctx, String path) {
    try {
      return ctx.read(path);
    } catch (PathNotFoundException e) {
      log.warn("path:{} not found", path, e);
      return null;
    }
  }

  @Data
  public static class Options {
    private String when;
    private Map<String, String> keysPath = new HashMap<>();
    private Map<String, Object> modifier;
  }
}

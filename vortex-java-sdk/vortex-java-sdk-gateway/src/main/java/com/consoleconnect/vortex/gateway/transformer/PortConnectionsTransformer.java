package com.consoleconnect.vortex.gateway.transformer;

import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.dto.OrganizationInfo;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Service
public class PortConnectionsTransformer extends AbstractResourceTransformer<Object> {

  protected final OrganizationService organizationService;

  public PortConnectionsTransformer(OrganizationService organizationService) {
    super(Object.class);
    this.organizationService = organizationService;
  }

  /**
   * process result and error code
   *
   * @param responseBody
   * @return
   */
  @Override
  public String doTransform(
      ServerWebExchange exchange,
      String responseBody,
      String customerId,
      TransformerApiProperty config,
      Object metadata) {

    Boolean isMgt = exchange.getAttribute(IamConstants.X_VORTEX_MGMT_ORG);

    if (isMgt == null || isMgt) {
      return responseBody;
    }

    OrganizationInfo org = organizationService.findOne(customerId);

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseBody);
    List<Map<String, Object>> resPortConns = ctx.read(config.getResponseBodyPath());
    // reset customer organization name
    for (int i = 0; i < resPortConns.size(); i++) {
      ctx.set("$.results[" + i + "].destCompany.name", org.getName());
      ctx.set("$.results[" + i + "].destCompany.company.registeredName", org.getName());
    }

    return ctx.jsonString();
  }

  @Override
  public String getTransformerId() {
    return "port.connection.list";
  }
}

package com.consoleconnect.vortex.gateway.transformer;

import com.auth0.json.mgmt.organizations.Organization;
import com.consoleconnect.vortex.gateway.config.TransformerApiProperty;
import com.consoleconnect.vortex.gateway.toolkit.JsonPathToolkit;
import com.consoleconnect.vortex.iam.model.IamConstants;
import com.consoleconnect.vortex.iam.service.OrganizationService;
import com.jayway.jsonpath.DocumentContext;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Service
@AllArgsConstructor
public class PortConnectionsTransformer extends AbstractResourceTransformer {

  protected final OrganizationService organizationService;

  /**
   * process result and error code
   *
   * @param responseBody
   * @return
   */
  @Override
  public byte[] doTransform(
      ServerWebExchange exchange,
      byte[] responseBody,
      String customerId,
      TransformerApiProperty config) {

    Boolean isMgt = exchange.getAttribute(IamConstants.X_VORTEX_MGMT_ORG);

    if (isMgt == null || isMgt) {
      return responseBody;
    }

    Organization org = organizationService.findOne(customerId);

    String responseJson = new String(responseBody, StandardCharsets.UTF_8);

    DocumentContext ctx = JsonPathToolkit.createDocCtx(responseJson);
    List<Map<String, Object>> resPortConns = ctx.read(config.getResponseBodyPath());
    // reset customer organization name
    for (int i = 0; i < resPortConns.size(); i++) {
      ctx.set("$.results[" + i + "].destCompany.name", org.getName());
      ctx.set("$.results[" + i + "].destCompany.company.registeredName", org.getName());
    }

    return ctx.jsonString().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String getTransformerId() {
    return "port.connection.list";
  }
}

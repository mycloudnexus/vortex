package com.consoleconnect.vortex.gateway.toolkit;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpELEngineTest {
  @Test
  void testIt() {
    Map<String, Object> data = new HashMap<>();

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("buyerId", "100");
    data.put("query", queryParams);

    Map<String, Object> body = new HashMap<>();
    body.put("action", "create");
    data.put("body", body);

    SpELEngine engine = new SpELEngine(data);
    Assertions.assertTrue(engine.isTrue("${body.action == 'create' && query.buyerId == '100'}"));
    Assertions.assertTrue(engine.isTrue("${body.action == 'create' || query.buyerId == '200'}"));
    Assertions.assertFalse(engine.isTrue("${body.action == 'create' && query.buyerId == '200'}"));
  }

  @Test
  void testIt2() {
    Map<String, Object> data = new HashMap<>();
    data.put("id", "5");

    Map<String, Object> context = new HashMap<>();
    context.put("orderIds", Arrays.asList("1", "2", "3"));
    context.put("resourceIds", Arrays.asList("4", "5", "6"));
    context.put("resources", Arrays.asList(data));
    context.put("data", data);

    Assertions.assertTrue(
        SpELEngine.isTrue(
            "${orderIds.contains(data.id) || resourceIds.contains(data.id) }", context));
  }

  @Test
  void testIt3() {
    Map<String, Object> data = new HashMap<>();
    data.put("id", "5");

    Map<String, Object> resource1 = new HashMap<>();
    resource1.put("id", "1");

    Map<String, Object> resource2 = new HashMap<>();
    resource2.put("id", "2");

    Map<String, Object> context = new HashMap<>();
    context.put("orderIds", Arrays.asList("1", "2", "3"));
    context.put("resources", Arrays.asList(resource1, resource2));

    SpELEngine spELEngine = new SpELEngine(context);
    Object result = spELEngine.evaluate("${resources.?[#orderIds.contains(id)]}", Object.class);

    System.out.println(JsonToolkit.toJson(result));
  }
}

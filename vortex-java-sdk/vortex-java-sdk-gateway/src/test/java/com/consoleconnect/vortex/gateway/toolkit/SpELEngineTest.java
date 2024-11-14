package com.consoleconnect.vortex.gateway.toolkit;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import java.util.*;
import lombok.Data;
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

    Resource resource1 = new Resource();
    resource1.setId("1");

    Resource resource2 = new Resource();
    resource2.setId("2");

    Map<String, Object> context1 = new HashMap<>();
    //    ExpressionParser parser = new SpelExpressionParser();

    context1.put("orderIds", Arrays.asList("1", "4", "3"));
    context1.put("resources", Arrays.asList(resource1, resource2));
    Object result1 =
        SpELEngine.evaluate("#resources.?[#orderIds.contains(id)]", context1, Object.class);
    System.out.println(JsonToolkit.toJson(result1));
  }

  @Data
  public static class Resource {
    private String id;
  }

  @Test
  void testSpELFiltering() {

    TestResource resource1 = new TestResource("1");

    TestResource resource2 = new TestResource("2");

    Map<String, Object> context = new HashMap<>();
    context.put("orderIds", Arrays.asList("1", "2", "3"));
    context.put("resources", Arrays.asList(resource1, resource2));

    SpELEngine spELEngine = new SpELEngine(context);
    Object result = spELEngine.evaluate("${resources.?[id in orderIds]}", Object.class);

    System.out.println(JsonToolkit.toJson(result));
  }

  @Data
  static class TestResource {
    private String id;

    public TestResource(String id) {
      this.id = id;
    }
  }
}

package com.consoleconnect.vortex.gateway.toolkit;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.data.IdObject;
import com.consoleconnect.vortex.gateway.data.ListResponse;
import com.consoleconnect.vortex.iam.enums.UserTypeEnum;
import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpelExpressionEngineTest {

  @Test
  void testFilterData() {
    // given
    ListResponse listResponse = new ListResponse();

    List<Object> results = new ArrayList<>();
    results.add(new IdObject("1"));
    results.add(new IdObject("2"));
    results.add(new IdObject("3"));
    results.add(new IdObject("4"));
    results.add(new IdObject("5"));
    listResponse.setResults(results);

    // filter list by id in resourceIds
    String expression = "#data.?[#resourceIds.contains(id)]";

    Map<String, Object> variables = new HashMap<>();
    variables.put(
        "data", JsonPathToolkit.read(JsonToolkit.toJson(listResponse), "$.results", Object.class));

    // resourceIds
    variables.put("resourceIds", List.of("1", "3", "5", "6"));

    // Act
    List<IdObject> filteredResults =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(SpelExpressionEngine.evaluate(expression, variables, Object.class)),
            new TypeReference<>() {});

    // Verify
    Assertions.assertEquals(3, filteredResults.size());
    Assertions.assertEquals("1", filteredResults.get(0).getId());
    Assertions.assertEquals("3", filteredResults.get(1).getId());
    Assertions.assertEquals("5", filteredResults.get(2).getId());

    // resourceIds
    variables.put("resourceIds", List.of());
    // verify
    filteredResults =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(SpelExpressionEngine.evaluate(expression, variables, Object.class)),
            new TypeReference<>() {});
    Assertions.assertEquals(0, filteredResults.size());

    // resourceIds
    variables.put("resourceIds", List.of("10"));
    // verify
    filteredResults =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(SpelExpressionEngine.evaluate(expression, variables, Object.class)),
            new TypeReference<>() {});
    Assertions.assertEquals(0, filteredResults.size());
  }

  @Test
  void testJsonObject() throws IOException {
    Map<String, Object> variables = new HashMap<>();
    variables.put("customerName", "test");
    variables.put("customerId", "1");

    String expression = AbstractIntegrationTest.readFileToString("spel/evaluate-object.json");
    Map<String, Object> data = JsonToolkit.fromJson(expression, new TypeReference<>() {});
    Object result = SpelExpressionEngine.evaluate(data, variables, Object.class);
    String resultJson = JsonToolkit.toJson(result);
    Assertions.assertEquals("test", JsonPathToolkit.read(resultJson, "$.name"));
    Assertions.assertEquals("test", JsonPathToolkit.read(resultJson, "$.company.registeredName"));
  }

  @Test
  void testBooleanExpression() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("customerName", "test");
    variables.put("customerId", 1);
    variables.put("userType", UserTypeEnum.CUSTOMER_USER.name());

    Assertions.assertEquals(
        Boolean.TRUE,
        SpelExpressionEngine.evaluate("#userType == 'CUSTOMER_USER'", variables, Boolean.class));
    Assertions.assertEquals(
        Boolean.TRUE, SpelExpressionEngine.evaluate("#customerId == 1", variables, Boolean.class));
    Assertions.assertNotEquals(
        Boolean.TRUE,
        SpelExpressionEngine.evaluate("#customerName == 'admin'", variables, Boolean.class));
  }

  @Test
  void testArrayContains() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("resourceIds", List.of("1", "3", "5", "6"));
    variables.put("portId", "3");

    Assertions.assertEquals(
        Boolean.TRUE,
        SpelExpressionEngine.evaluate("#resourceIds.contains(#portId)", variables, Boolean.class));

    variables.put("portId", "10");
    Assertions.assertEquals(
        Boolean.FALSE,
        SpelExpressionEngine.evaluate("#resourceIds.contains(#portId)", variables, Boolean.class));
  }
}

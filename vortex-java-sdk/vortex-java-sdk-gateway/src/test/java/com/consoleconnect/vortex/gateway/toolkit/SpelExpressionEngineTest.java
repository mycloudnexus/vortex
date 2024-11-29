package com.consoleconnect.vortex.gateway.toolkit;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.data.IdObject;
import com.consoleconnect.vortex.gateway.data.ListResponse;
import com.fasterxml.jackson.core.type.TypeReference;
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
            JsonToolkit.toJson(SpelExpressionEngine.parse(expression, variables)),
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
            JsonToolkit.toJson(SpelExpressionEngine.parse(expression, variables)),
            new TypeReference<>() {});
    Assertions.assertEquals(0, filteredResults.size());

    // resourceIds
    variables.put("resourceIds", List.of("10"));
    // verify
    filteredResults =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(SpelExpressionEngine.parse(expression, variables)),
            new TypeReference<>() {});
    Assertions.assertEquals(0, filteredResults.size());
  }
}

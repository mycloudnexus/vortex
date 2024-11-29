package com.consoleconnect.vortex.gateway.toolkit;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import com.consoleconnect.vortex.gateway.data.IdObject;
import com.jayway.jsonpath.DocumentContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonPathToolkitTest {

  @Test
  void givenJsonString_whenReadJsonPath_shouldBeOk() {

    Map<String, Object> data = new HashMap<>();
    data.put("id", 1);
    data.put("name", "test");
    data.put("id1", new IdObject("id1"));
    data.put("list", List.of("test1", "test2"));
    data.put("map", Map.of("key1", "value1", "key2", "value2"));

    String json = JsonToolkit.toJson(data);

    Assertions.assertEquals("1", JsonPathToolkit.read(json, "$.id"));
    Assertions.assertEquals("test", JsonPathToolkit.read(json, "$.name"));
    Assertions.assertEquals("id1", JsonPathToolkit.read(json, "$.id1.id"));
    Assertions.assertEquals("value1", JsonPathToolkit.read(json, "$.map.key1", Object.class));
    Assertions.assertNotNull(JsonPathToolkit.read(json, "$.list", Object.class));
    Assertions.assertNotNull(JsonPathToolkit.read(json, "$.map", Object.class));
  }

  @Test
  void givenJsonString_whenUpdatePath_shouldBeOk() {

    Map<String, Object> data = new HashMap<>();
    data.put("id", 1);
    data.put("name", "test");
    data.put("id1", new IdObject("id1"));
    data.put("list", List.of("test1", "test2"));
    data.put("map", Map.of("key1", "value1", "key2", "value2"));

    String json = JsonToolkit.toJson(data);

    DocumentContext context = JsonPathToolkit.createDocCtx(json);
    context.set("$.id", "2");
    context.set("$.name", "test2");
    context.set("$.id1.id", "id2");
    context.set("$.list[0]", "test3");
    context.set("$.map.key1", "value3");

    json = context.jsonString();

    Assertions.assertEquals("2", JsonPathToolkit.read(json, "$.id"));
    Assertions.assertEquals("test2", JsonPathToolkit.read(json, "$.name"));
    Assertions.assertEquals("id2", JsonPathToolkit.read(json, "$.id1.id"));
    Assertions.assertEquals("value3", JsonPathToolkit.read(json, "$.map.key1", Object.class));
    Assertions.assertEquals("value2", JsonPathToolkit.read(json, "$.map.key2", Object.class));
    Assertions.assertNotNull(JsonPathToolkit.read(json, "$.list", Object.class));
    Assertions.assertNotNull(JsonPathToolkit.read(json, "$.map", Object.class));
  }
}

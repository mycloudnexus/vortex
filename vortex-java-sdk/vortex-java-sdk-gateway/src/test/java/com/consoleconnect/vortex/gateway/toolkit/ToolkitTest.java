package com.consoleconnect.vortex.gateway.toolkit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ToolkitTest {

  @Test
  void testJsonPathToolkit() {
    String json = "{\"id\":1}";
    JsonPathToolkit.createDocCtx(json);
    Integer ret = JsonPathToolkit.read(json, "$.id", JsonPathToolkit.CONFIGURATION, Integer.class);
    Assertions.assertEquals(1, ret);
  }
}

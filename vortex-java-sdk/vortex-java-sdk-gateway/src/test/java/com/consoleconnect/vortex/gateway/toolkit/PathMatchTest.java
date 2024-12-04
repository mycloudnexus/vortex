package com.consoleconnect.vortex.gateway.toolkit;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

class PathMatchTest {

  @Test
  void testIt() {
    // Define the pattern
    String pattern = "/api/company/ports/{portId}/connections";
    // Define the actual path to match
    String path = "/api/company/ports/12345/connections";

    // Create an instance of AntPathMatcher
    AntPathMatcher matcher = new AntPathMatcher();

    // Check if the path matches the pattern
    Assertions.assertTrue(matcher.match(pattern, path));

    // Extract the variables
    Map<String, String> variables = matcher.extractUriTemplateVariables(pattern, path);
    // Print the extracted variable
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }
    System.out.println("portId: " + variables.get("portId"));
    Assertions.assertEquals("12345", variables.get("portId"));
  }
}

package com.consoleconnect.vortex.gateway.toolkit;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

class PathMatcherToolkitTest {

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

  @Test
  void testMatch() {

    Assertions.assertTrue(
        PathMatcherToolkit.findFirstMatch(List.of("/api/auth/token"), "/api/auth/token")
            .isPresent());

    Assertions.assertTrue(
        PathMatcherToolkit.findFirstMatch(List.of("/api/auth/token"), "/api/auth").isEmpty());

    Optional<String> matchedPath =
        PathMatcherToolkit.findFirstMatch(
            List.of("/api/{username}/ports", "/api/{username}/ports/{portId}"), "/api/123/ports");
    Assertions.assertTrue(matchedPath.isPresent());
    Assertions.assertEquals("/api/{username}/ports", matchedPath.get());

    matchedPath =
        PathMatcherToolkit.findFirstMatch(
            List.of("/api/{username}/ports", "/api/{username}/ports/{portId}"),
            "/api/123/ports/456");
    Assertions.assertTrue(matchedPath.isPresent());
    Assertions.assertEquals("/api/{username}/ports/{portId}", matchedPath.get());

    matchedPath =
        PathMatcherToolkit.findFirstMatch(
            List.of("/api/{username}/ports", "/api/{username}/ports/{portId}"),
            "/api/123/ports/456/connections");
    Assertions.assertTrue(matchedPath.isEmpty());

    matchedPath =
        PathMatcherToolkit.findFirstMatch(
            List.of(
                "/api/{username}/ports",
                "/api/{username}/ports/{portId}",
                "/api/{username}/ports/**"),
            "/api/123/ports/456/connections");
    Assertions.assertTrue(matchedPath.isPresent());
    Assertions.assertEquals("/api/{username}/ports/**", matchedPath.get());
  }
}

package com.consoleconnect.vortex.gateway.toolkit;

import java.util.List;
import java.util.Optional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class PathMatcherToolkit {

  private PathMatcherToolkit() {}

  private static final PathMatcher pathMatcher =
      new AntPathMatcher(); // Default Spring implementation

  public static Optional<String> findFirstMatch(List<String> patterns, String path) {
    return patterns.stream().filter(pattern -> pathMatcher.match(pattern, path)).findFirst();
  }
}

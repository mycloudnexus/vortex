package com.consoleconnect.vortex.gateway.model;

import com.consoleconnect.vortex.gateway.dto.PathAccessRule;
import com.consoleconnect.vortex.gateway.enums.AccessActionEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PathAccessRuleTable {

  private final Map<String, List<PathAccessRule>> method2Rules;

  public List<PathAccessRule> get(String method) {
    return method2Rules.getOrDefault(method, new ArrayList<>());
  }

  public List<String> getPaths(String method, AccessActionEnum action) {
    return get(method).stream()
        .filter(rule -> rule.getAction() == action)
        .map(PathAccessRule::getPath)
        .toList();
  }

  public static PathAccessRuleTable build(List<PathAccessRule> rules) {
    Map<String, List<PathAccessRule>> method2Rules = new HashMap<>();
    for (PathAccessRule rule : rules) {
      List<PathAccessRule> methodRules =
          method2Rules.computeIfAbsent(rule.getMethod(), k -> new ArrayList<>());
      methodRules.add(rule);
    }

    return new PathAccessRuleTable(method2Rules);
  }
}

package com.consoleconnect.vortex.gateway.model;

import java.util.Map;
import lombok.Data;

@Data
public class Hook<T> {
  private String id;
  private String name;
  private T options;

  public static final class Default extends Hook<Map<String, Object>> {}
}

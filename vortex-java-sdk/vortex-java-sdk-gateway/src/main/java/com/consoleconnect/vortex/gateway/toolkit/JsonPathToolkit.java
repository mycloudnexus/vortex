package com.consoleconnect.vortex.gateway.toolkit;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class JsonPathToolkit {

  private static final Configuration DEFAULT_CONFIGURATION = Configuration.builder().build();

  private static final Configuration CONFIGURATION =
      Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

  public static String read(String json, String path) {
    return read(json, path, String.class);
  }

  public static <T> T read(String json, String path, Class<T> clazz) {
    return JsonPath.parse(json, CONFIGURATION).read(path, clazz);
  }

  public static <T> T read(String json, String path, Configuration conf, Class<T> clazz) {
    return JsonPath.parse(json, conf).read(path, clazz);
  }

  public static DocumentContext createDocCtx(String json) {
    return JsonPath.using(DEFAULT_CONFIGURATION).parse(json);
  }
}

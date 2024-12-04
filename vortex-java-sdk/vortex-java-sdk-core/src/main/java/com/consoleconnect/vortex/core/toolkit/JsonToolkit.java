package com.consoleconnect.vortex.core.toolkit;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonToolkit {

  private JsonToolkit() {}

  public static ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new JodaModule());

    return objectMapper;
  }

  private static final TypeReference<Map<String, String>> STRING_MAP_TYPE_REFERENCE =
      new TypeReference<Map<String, String>>() {};

  public static String toJson(Object data) {
    if (data == null) {
      return null;
    }
    try {
      return createObjectMapper().writeValueAsString(data);
    } catch (JsonProcessingException ex) {
      throw VortexException.internalError(ex.getMessage());
    }
  }

  public static <T> T fromJson(String json, Class<T> classOfT) {
    try {
      return createObjectMapper().readValue(json, classOfT);
    } catch (JsonProcessingException ex) {
      throw VortexException.internalError(ex.getMessage());
    }
  }

  public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
    try {
      return createObjectMapper().readValue(json, valueTypeRef);
    } catch (JsonProcessingException ex) {
      throw VortexException.internalError(ex.getMessage());
    }
  }

  public static String toPrettyJson(Object data) {
    try {
      return createObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data);
    } catch (JsonProcessingException ex) {
      throw VortexException.internalError(ex.getMessage());
    }
  }

  public static Map<String, String> toMap(String json) {
    return fromJson(json, STRING_MAP_TYPE_REFERENCE);
  }

  public static String parseValue(String path, String json) {
    DocumentContext jsonContext = JsonPath.parse(json);
    return jsonContext.read(path);
  }

  // Method to merge two JsonNodes deeply
  public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
    if (mainNode.isObject() && updateNode.isObject()) {
      ObjectNode mainObjectNode = (ObjectNode) mainNode;
      updateNode
          .fields()
          .forEachRemaining(
              field -> {
                String fieldName = field.getKey();
                JsonNode valueInUpdate = field.getValue();

                if (mainObjectNode.has(fieldName)) {
                  JsonNode valueInMain = mainObjectNode.get(fieldName);
                  // If both are objects, merge them recursively
                  if (valueInMain.isObject() && valueInUpdate.isObject()) {
                    JsonNode mergedChild = merge(valueInMain, valueInUpdate);
                    mainObjectNode.set(fieldName, mergedChild);
                  } else {
                    // Overwrite if not both objects
                    mainObjectNode.set(fieldName, valueInUpdate);
                  }
                } else {
                  // Add the new field
                  mainObjectNode.set(fieldName, valueInUpdate);
                }
              });
    }
    return mainNode;
  }

  public static Optional<String> merge(String mainJson, String updateJson) {
    try {
      ObjectMapper mapper = createObjectMapper();
      JsonNode mainNode = mapper.readTree(mainJson);
      JsonNode updateNode = mapper.readTree(updateJson);
      JsonNode mergedNode = merge(mainNode, updateNode);
      return Optional.of(mapper.writeValueAsString(mergedNode));
    } catch (Exception ex) {
      log.warn("Failed to merge json strings", ex);
      return Optional.empty();
    }
  }

  public static String formatJson(String json) {
    try {
      ObjectMapper mapper = createObjectMapper();
      Object obj = mapper.readValue(json, Object.class);
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (Exception ex) {
      log.warn("Failed to format json", ex);
      return json;
    }
  }
}

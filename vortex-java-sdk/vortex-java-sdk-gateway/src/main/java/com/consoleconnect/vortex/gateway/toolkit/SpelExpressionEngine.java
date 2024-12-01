package com.consoleconnect.vortex.gateway.toolkit;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.integration.json.JsonPropertyAccessor;

@Slf4j
public class SpelExpressionEngine {

  private SpelExpressionEngine() {}

  public static StandardEvaluationContext buildContext(Map<String, Object> variables) {
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.addPropertyAccessor(new MapAccessor());
    context.addPropertyAccessor(new JsonPropertyAccessor());
    context.setVariables(variables);
    GenericConversionService conversionService = new DefaultConversionService();
    conversionService.addConverter(new ConvertLinkedHashMapToString());
    context.setTypeConverter(new StandardTypeConverter(conversionService));
    return context;
  }

  public static class ConvertLinkedHashMapToString
      implements Converter<LinkedHashMap<?, ?>, String> {
    @Override
    public String convert(LinkedHashMap<?, ?> source) {
      return escape(JsonToolkit.toJson(source));
    }

    private String escape(String raw) {
      String escaped = raw;
      escaped = escaped.replace("\\", "\\\\");
      escaped = escaped.replace("\"", "\\\"");
      escaped = escaped.replace("\b", "\\b");
      escaped = escaped.replace("\f", "\\f");
      escaped = escaped.replace("\n", "\\n");
      escaped = escaped.replace("\r", "\\r");
      escaped = escaped.replace("\t", "\\t");
      return escaped;
    }
  }

  @SuppressWarnings("unchecked")
  public static Object evaluate(Object expression, Map<String, Object> context) {
    if (expression instanceof String) {
      return evaluate((String) expression, context);
    } else if (expression instanceof Map<?, ?>) {
      return evaluate((Map<String, Object>) expression, context);
    } else {
      throw VortexException.badRequest("Unsupported type: " + expression.getClass());
    }
  }

  public static Object evaluate(String expression, Map<String, Object> context) {
    try {
      return new SpelExpressionParser()
          .parseExpression(expression)
          .getValue(buildContext(context), Object.class);
    } catch (Exception ex) {
      log.error("Failed to evaluate expression: {}", expression, ex);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static Object evaluate(Map<String, Object> map, Map<String, Object> context) {
    if (map == null) {
      return null;
    }
    for (Map.Entry<String, Object> v : map.entrySet()) {

      if (v.getValue() instanceof String value) {
        v.setValue(evaluate(value, context));
      } else if (v.getValue() instanceof Map<?, ?>) {
        v.setValue(evaluate((Map<String, Object>) v.getValue(), context));
      } else {
        throw VortexException.badRequest("Unsupported type: " + v.getValue().getClass());
      }
    }
    return map;
  }
}

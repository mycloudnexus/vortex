package com.consoleconnect.vortex.gateway.toolkit;

import com.consoleconnect.vortex.core.toolkit.JsonToolkit;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.integration.json.JsonPropertyAccessor;

@Slf4j
public class SpELEngine {

  private static final ExpressionParser expressionParser = new SpelExpressionParser();

  private final StandardEvaluationContext evaluationContext;

  private final ParserContext parserContext =
      new ParserContext() {
        @Override
        public boolean isTemplate() {
          return true;
        }

        @Override
        public String getExpressionPrefix() {
          return "${";
        }

        @Override
        public String getExpressionSuffix() {
          return "}";
        }
      };

  public SpELEngine(Map<String, Object> variables) {
    evaluationContext = new StandardEvaluationContext(variables);
    evaluationContext.addPropertyAccessor(new JsonPropertyAccessor());
    evaluationContext.addPropertyAccessor(new MapAccessor());

    GenericConversionService conversionService = new DefaultConversionService();
    conversionService.addConverter(new ConvertLinkedHashMapToString());
    evaluationContext.setTypeConverter(new StandardTypeConverter(conversionService));
  }

  public boolean isTrue(String expression) {
    return Boolean.TRUE.equals(evaluate(expression, Boolean.class));
  }

  public Object evaluate(String expression) {
    return evaluate(expression, Object.class);
  }

  public <T> T evaluate(String expression, Class<T> clazz) {
    log.info("Evaluating expression: {}", expression);
    try {
      return expressionParser
          .parseExpression(expression, parserContext)
          .getValue(evaluationContext, clazz);
    } catch (Exception ex) {
      log.error("Error evaluating expression: {}", expression, ex);
      return null;
    }
  }

  public static boolean isTrue(String expression, Map<String, Object> variables) {
    return new SpELEngine(variables).isTrue(expression);
  }

  public static <T> T evaluate(String expression, Map<String, Object> variables, Class<T> clazz) {
    SpELEngine spELEngine = new SpELEngine(variables);
    return spELEngine.evaluate(expression, clazz);
  }

  public static class ConvertLinkedHashMapToString
      implements Converter<LinkedHashMap<?, ?>, String> {
    @Override
    public String convert(LinkedHashMap<?, ?> source) {
      return escape(JsonToolkit.toJson(source));
    }
  }

  public static String escape(String raw) {
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

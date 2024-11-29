package com.consoleconnect.vortex.gateway.toolkit;

import java.util.Map;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelExpressionEngine {
  private SpelExpressionEngine() {}

  public static Object parse(String expression, Map<String, Object> variables) {
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.addPropertyAccessor(new MapAccessor());
    context.setVariables(variables);
    ExpressionParser parser = new SpelExpressionParser();
    return parser.parseExpression(expression).getValue(context);
  }
}

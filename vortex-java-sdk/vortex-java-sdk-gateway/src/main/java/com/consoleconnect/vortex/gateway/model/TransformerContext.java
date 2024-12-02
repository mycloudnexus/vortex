package com.consoleconnect.vortex.gateway.model;

import com.consoleconnect.vortex.iam.enums.UserTypeEnum;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
public class TransformerContext<T> {
  private String customerId;
  private UserTypeEnum loginUserType;
  private TransformerSpecification<T> specification;
  private Map<String, Object> variables;

  private HttpMethod httpMethod;
  private String path;

  private List<Object> data; // data to be transformed
}

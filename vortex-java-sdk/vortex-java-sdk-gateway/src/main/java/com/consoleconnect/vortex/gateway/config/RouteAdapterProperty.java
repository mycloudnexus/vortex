package com.consoleconnect.vortex.gateway.config;

import java.util.List;
import lombok.Data;

@Data
public class RouteAdapterProperty {
  private String adapterClassName;
  private List<ApiProperty> apis;
}

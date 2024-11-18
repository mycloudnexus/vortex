package com.consoleconnect.vortex.cc.model;

import lombok.Data;

@Data
public class Heartbeat {
  private String name;
  private long now;
  private long uptime;
  private String humanUptime;
  private long uptimeSeconds;
  private String version;
}

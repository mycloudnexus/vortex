package com.consoleconnect.vortex.cc;

import feign.slf4j.Slf4jLogger;

public class ConsoleConnectSlf4jLogger extends Slf4jLogger {

  public ConsoleConnectSlf4jLogger() {
    super(ConsoleConnectSlf4jLogger.class);
  }

  @Override
  protected void log(String configKey, String format, Object... args) {

    // don't log the token
    if (format.equalsIgnoreCase("%s: %s") && "Authorization".equalsIgnoreCase((String) args[0])) {
      return;
    }

    super.log(configKey, format, args);
  }
}

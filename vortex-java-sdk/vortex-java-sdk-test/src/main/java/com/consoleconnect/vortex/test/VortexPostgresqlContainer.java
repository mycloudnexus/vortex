package com.consoleconnect.vortex.test;

import org.testcontainers.containers.PostgreSQLContainer;

public class VortexPostgresqlContainer extends PostgreSQLContainer<VortexPostgresqlContainer> {

  private static final String IMAGE_VERSION = "postgres:14.9";
  private static VortexPostgresqlContainer container;

  public VortexPostgresqlContainer() {
    super(IMAGE_VERSION);
  }

  public static VortexPostgresqlContainer getInstance() {
    if (container == null) {
      container = new VortexPostgresqlContainer();
      container.addFixedExposedPort(6432, 5432);
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    System.setProperty("spring.datasource.url", container.getJdbcUrl());
    System.setProperty("spring.datasource.username", container.getUsername());
    System.setProperty("spring.datasource.password", container.getPassword());
  }

  @Override
  public void stop() {
    // do nothing, JVM handles shut down
  }
}

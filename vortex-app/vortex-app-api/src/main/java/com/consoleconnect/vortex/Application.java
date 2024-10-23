package com.consoleconnect.vortex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.consoleconnect.vortex")
@EnableJpaRepositories(basePackages = "com.consoleconnect.vortex")
@EntityScan(basePackages = "com.consoleconnect.vortex")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}

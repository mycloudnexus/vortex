package com.consoleconnect.vortex.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.exception.VortexExceptionHandler;
import com.consoleconnect.vortex.gateway.TestApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class)
class VortexExceptionHandlerTest {

  @Autowired VortexExceptionHandler vortexExceptionHandler;

  @Test
  void testVortexExceptionHandler() {
    Object ret =
        vortexExceptionHandler.generateBody(
            HttpStatus.BAD_REQUEST, null, VortexException.badRequest("bad"));
    assertNotNull(ret);
  }
}

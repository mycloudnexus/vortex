package com.consoleconnect.vortex.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.exception.VortexExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;

class VortexExceptionHandlerTest {

  @Test
  void testVortexExceptionHandler() {
    VortexExceptionHandler handler =
        new VortexExceptionHandler(
            new DefaultErrorAttributes(),
            new WebProperties.Resources(),
            new DefaultServerCodecConfigurer(),
            new AnnotationConfigApplicationContext());
    Object ret =
        handler.generateBody(HttpStatus.BAD_REQUEST, null, VortexException.badRequest("bad"));
    assertNotNull(ret);
  }
}

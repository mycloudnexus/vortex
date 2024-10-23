package com.consoleconnect.vortex.test;

import org.reactivestreams.Publisher;

@FunctionalInterface
public interface ClientDelegate<T> {

  Publisher<T> call();
}

package com.consoleconnect.vortex.e2e.user;

import com.intuit.karate.junit5.Karate;

class UserRunner {

  @Karate.Test
  Karate testOrganization() {
    return Karate.run("users").relativeTo(getClass());
  }
}

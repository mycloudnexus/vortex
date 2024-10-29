package com.consoleconnect.vortex.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

class E2ETest {


    @Test
    void testParallel() {
        Results results = Runner.path("classpath:com/consoleconnect/vortex/e2e")
                //.outputCucumberJson(true)
                .parallel(5);
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

}

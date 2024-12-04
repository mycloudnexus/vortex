package com.consoleconnect.vortex.core.toolkit;

import com.consoleconnect.vortex.test.AbstractIntegrationTest;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonToolkitTest {
  @Test
  void testMergeJsons() throws IOException {
    String mainJson = AbstractIntegrationTest.readFileToString("data/merge-json/main.json");
    String updatingJson = AbstractIntegrationTest.readFileToString("data/merge-json/updating.json");

    Optional<String> mergedJsonOptional = JsonToolkit.merge(mainJson, updatingJson);
    Assertions.assertFalse(mergedJsonOptional.isEmpty());

    String expectedMergedJson =
        AbstractIntegrationTest.readFileToString("data/merge-json/merged.json");

    Assertions.assertEquals(
        JsonToolkit.formatJson(expectedMergedJson),
        JsonToolkit.formatJson(mergedJsonOptional.get()));
  }
}

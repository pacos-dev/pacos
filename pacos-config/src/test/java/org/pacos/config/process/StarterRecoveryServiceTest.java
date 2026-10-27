package org.pacos.config.process;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated("Reads JVM-wide properties used for starter recovery.")
class StarterRecoveryServiceTest {

    @Test
    void whenRecoveryArgumentsWerePropagatedThenRestoreOriginalArguments() {
        String originalValue = System.getProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        List<String> expected = List.of("-DworkingDir=/opt/pacos", "-Drmi.port=1999");
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(
                String.join(String.valueOf((char) 0), expected).getBytes(StandardCharsets.UTF_8));
        System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, encoded);

        try {
            assertEquals(expected, StarterRecoveryService.readArguments());
        } finally {
            restoreProperty(originalValue);
        }
    }

    private static void restoreProperty(String originalValue) {
        if (originalValue == null) {
            System.clearProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY);
        } else {
            System.setProperty(StarterRecoveryService.STARTER_ARGS_PROPERTY, originalValue);
        }
    }
}

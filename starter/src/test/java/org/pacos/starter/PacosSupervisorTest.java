package org.pacos.starter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.rmi.RemoteRegistryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PacosSupervisorTest {

    @Test
    void whenEngineExitsUnexpectedlyThenSupervisorStartsReplacement() {
        AtomicInteger runCount = new AtomicInteger();
        PacosProcess engineProcess = mock(PacosProcess.class);
        when(engineProcess.run()).thenAnswer(invocation -> {
            int run = runCount.incrementAndGet();
            if (run == 2) {
                // Stop the supervision loop after proving that a second process is run.
                Thread.currentThread().interrupt();
            }
            return run == 1 ? 17 : 0;
        });

        Pacos pacos = new Pacos(
                List.of(),
                null,
                () -> new AppArtifact("org.pacos", "engine", "1.0"),
                artifact -> engineProcess);

        try (MockedStatic<RemoteRegistryService> remoteRegistry = mockStatic(RemoteRegistryService.class)) {
            remoteRegistry.when(() -> RemoteRegistryService.registerRemoteInterface(any()))
                    .thenAnswer(invocation -> null);

            pacos.start();
        }

        assertEquals(2, runCount.get());
        verify(engineProcess, times(2)).run();
    }
}

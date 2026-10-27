package org.pacos.starter;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.rmi.RemoteRegistryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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

        try {
            try (MockedStatic<RemoteRegistryService> remoteRegistry = mockStatic(RemoteRegistryService.class)) {
                remoteRegistry.when(() -> RemoteRegistryService.registerRemoteInterface(any()))
                        .thenReturn(true);

                pacos.start();
            }

            assertEquals(2, runCount.get());
        } finally {
            // Do not leak the test's loop-stop interrupt into another test on this worker.
            Thread.interrupted();
        }
    }

    @Test
    void whenRestartIsRequestedThenSupervisorStartsOneReplacement() throws Exception {
        AtomicInteger factoryCalls = new AtomicInteger();
        AtomicInteger runCount = new AtomicInteger();
        CountDownLatch oldEngineStopped = new CountDownLatch(1);
        AtomicReference<Pacos> pacosReference = new AtomicReference<>();

        Pacos pacos = new Pacos(
                List.of(),
                null,
                () -> new AppArtifact("org.pacos", "engine", "1.0"),
                artifact -> {
                    factoryCalls.incrementAndGet();
                    PacosProcess process = mock(PacosProcess.class);
                    when(process.stopAndAwait()).thenAnswer(invocation -> {
                        oldEngineStopped.countDown();
                        return true;
                    });
                    when(process.run()).thenAnswer(invocation -> {
                        int run = runCount.incrementAndGet();
                        if (run == 1) {
                            pacosReference.get().restartEngine();
                            assertTrue(oldEngineStopped.await(3, TimeUnit.SECONDS));
                            return 0;
                        }
                        Thread.currentThread().interrupt();
                        return 0;
                    });
                    return process;
                });
        pacosReference.set(pacos);

        try {
            try (MockedStatic<RemoteRegistryService> remoteRegistry = mockStatic(RemoteRegistryService.class)) {
                remoteRegistry.when(() -> RemoteRegistryService.registerRemoteInterface(any()))
                        .thenReturn(true);

                pacos.start();
            }

            assertEquals(2, factoryCalls.get());
            assertEquals(2, runCount.get());
        } finally {
            Thread.interrupted();
        }
    }
}

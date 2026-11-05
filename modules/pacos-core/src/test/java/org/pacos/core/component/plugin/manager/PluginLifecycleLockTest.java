package org.pacos.core.component.plugin.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.dto.PluginDTO;

class PluginLifecycleLockTest {

    @Test
    void whenCallExecuteForSamePluginThenSerializeOperations() throws Exception {
        PluginLifecycleLock lifecycleLock = new PluginLifecycleLock();
        PluginDTO plugin = createPlugin("1.0");
        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondEntered = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<String> first = executor.submit(() -> lifecycleLock.execute(plugin, () -> {
                firstEntered.countDown();
                await(releaseFirst);
                return "first";
            }));
            assertTrue(firstEntered.await(5, TimeUnit.SECONDS));

            Future<String> second = executor.submit(() -> lifecycleLock.execute(plugin, () -> {
                secondEntered.countDown();
                return "second";
            }));

            assertFalse(secondEntered.await(100, TimeUnit.MILLISECONDS));
            releaseFirst.countDown();

            assertTrue(secondEntered.await(5, TimeUnit.SECONDS));
            assertEquals("first", first.get(5, TimeUnit.SECONDS));
            assertEquals("second", second.get(5, TimeUnit.SECONDS));
            assertEquals(0, lifecycleLock.activeLockCount());
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void whenCallExecuteForDifferentPluginsThenRunOperationsConcurrently() throws Exception {
        PluginLifecycleLock lifecycleLock = new PluginLifecycleLock();
        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondEntered = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<String> first = executor.submit(() -> lifecycleLock.execute(createPlugin("1.0"), () -> {
                firstEntered.countDown();
                await(releaseFirst);
                return "first";
            }));
            assertTrue(firstEntered.await(5, TimeUnit.SECONDS));

            Future<String> second = executor.submit(() -> lifecycleLock.execute(createPlugin("2.0"), () -> {
                secondEntered.countDown();
                return "second";
            }));

            assertTrue(secondEntered.await(5, TimeUnit.SECONDS));
            assertEquals("second", second.get(5, TimeUnit.SECONDS));
            releaseFirst.countDown();
            assertEquals("first", first.get(5, TimeUnit.SECONDS));
            assertEquals(0, lifecycleLock.activeLockCount());
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void whenCallExecuteAndOperationFailsThenReleaseLock() {
        PluginLifecycleLock lifecycleLock = new PluginLifecycleLock();
        PluginDTO plugin = createPlugin("1.0");
        IllegalStateException failure = new IllegalStateException("operation failed");

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> lifecycleLock.execute(plugin, () -> {
                    throw failure;
                })));

        assertEquals(0, lifecycleLock.activeLockCount());
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private static PluginDTO createPlugin(String version) {
        PluginDTO plugin = new PluginDTO();
        plugin.setGroupId("org.pacos");
        plugin.setArtifactName("sample");
        plugin.setVersion(version);
        return plugin;
    }
}

package org.pacos.core.component.plugin.manager;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.service.PluginService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

class PluginLifecycleExecutionTest {

    @Test
    void whenCallStartAndStopThroughManagerThenUseLifecycleExecutor() throws Exception {
        PluginDTO plugin = createPlugin("1.0");
        PluginState state = mock(PluginState.class);
        AtomicReference<Thread> startThread = new AtomicReference<>();
        AtomicReference<Thread> stopThread = new AtomicReference<>();
        when(state.canRun(plugin)).thenAnswer(invocation -> {
            startThread.set(Thread.currentThread());
            return false;
        });
        when(state.canStop(plugin)).thenAnswer(invocation -> {
            stopThread.set(Thread.currentThread());
            return false;
        });

        try (AnnotationConfigApplicationContext context = createAsyncContext(state, mock(PluginService.class))) {
            PluginManager manager = context.getBean(PluginManager.class);

            assertTrue(manager.startPlugin(plugin).get(5, SECONDS));
            assertTrue(manager.stopPlugin(plugin).get(5, SECONDS));

            assertNotSame(Thread.currentThread(), startThread.get());
            assertNotSame(Thread.currentThread(), stopThread.get());
            assertTrue(startThread.get().getName().startsWith("PluginLifecycleTest-"));
            assertTrue(stopThread.get().getName().startsWith("PluginLifecycleTest-"));
        }
    }

    @Test
    void whenCallStartupThenRegisterCatalogAndAwaitEnabledPluginsWithoutFillingQueue() throws Exception {
        List<PluginDTO> plugins = IntStream.range(0, 25)
                .mapToObj(version -> createPlugin("1." + version)).toList();
        PluginState state = spy(new PluginState());
        PluginService service = mock(PluginService.class);
        when(service.findNotRemovedPlugin()).thenReturn(plugins);
        when(service.findEnabledPlugin()).thenReturn(plugins);
        CountDownLatch startEntered = new CountDownLatch(1);
        CountDownLatch releaseStart = new CountDownLatch(1);
        doAnswer(invocation -> {
            assertEquals(plugins.size(), state.getPlugins().size());
            startEntered.countDown();
            assertTrue(releaseStart.await(5, SECONDS));
            return false;
        }).when(state).canRun(any(PluginDTO.class));

        try (AnnotationConfigApplicationContext context = createAsyncContext(state, service);
             var caller = Executors.newSingleThreadExecutor()) {
            try {
                PluginManager manager = context.getBean(PluginManager.class);
                Future<?> initialization = caller.submit(manager::initializePluginsOnApplicationReadyEvent);

                assertTrue(startEntered.await(5, SECONDS));
                assertThrows(TimeoutException.class, () -> initialization.get(100, MILLISECONDS));
                releaseStart.countDown();
                initialization.get(5, SECONDS);

                verify(state, times(plugins.size())).canRun(any(PluginDTO.class));
            } finally {
                releaseStart.countDown();
            }
        }
    }

    @Test
    void whenCallStartStopAndRemoveForSamePluginThenSerializeOperationsAndReleaseLock() throws Exception {
        PluginDTO plugin = createPlugin("1.0");
        PluginDTO samePlugin = createPlugin("1.0");
        PluginState state = spy(new PluginState());
        PluginLifecycle lifecycle = new PluginLifecycle(mock(SwaggerUIConfigReload.class),
                mock(ApplicationContext.class), state);
        PluginManager manager = new PluginManager(mock(PluginService.class), state, lifecycle);
        manager.addPlugin(plugin);
        CountDownLatch startEntered = new CountDownLatch(1);
        CountDownLatch releaseStart = new CountDownLatch(1);
        CountDownLatch stopRequested = new CountDownLatch(1);
        CountDownLatch removalRequested = new CountDownLatch(1);
        doAnswer(invocation -> {
            startEntered.countDown();
            assertTrue(releaseStart.await(5, SECONDS));
            return false;
        }).when(state).canRun(plugin);

        try (var callers = Executors.newFixedThreadPool(3)) {
            try {
                Future<Boolean> start = callers.submit(() -> manager.startPlugin(plugin).join());
                assertTrue(startEntered.await(5, SECONDS));
                Future<Boolean> stop = callers.submit(() -> {
                    stopRequested.countDown();
                    return manager.stopPlugin(samePlugin).join();
                });
                Future<?> removal = callers.submit(() -> {
                    removalRequested.countDown();
                    manager.removePlugin(samePlugin);
                });

                assertTrue(stopRequested.await(5, SECONDS));
                assertTrue(removalRequested.await(5, SECONDS));
                assertThrows(TimeoutException.class, () -> stop.get(100, MILLISECONDS));
                assertThrows(TimeoutException.class, () -> removal.get(100, MILLISECONDS));
                assertEquals(PluginStatusEnum.OFF, state.getState(plugin));
                releaseStart.countDown();

                assertTrue(start.get(5, SECONDS));
                assertTrue(stop.get(5, SECONDS));
                removal.get(5, SECONDS);
                assertNull(state.getState(plugin));
                Map<?, ?> locks = (Map<?, ?>) ReflectionTestUtils.getField(lifecycle, "lifecycleLocks");
                assertTrue(locks.isEmpty());
            } finally {
                releaseStart.countDown();
            }
        }
    }

    private AnnotationConfigApplicationContext createAsyncContext(PluginState state, PluginService service) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AsyncConfiguration.class, PluginManager.class, PluginLifecycle.class);
        context.registerBean(PluginState.class, () -> state);
        context.registerBean(PluginService.class, () -> service);
        context.registerBean(SwaggerUIConfigReload.class, () -> mock(SwaggerUIConfigReload.class));
        context.refresh();
        return context;
    }

    private PluginDTO createPlugin(String version) {
        PluginDTO plugin = new PluginDTO();
        plugin.setGroupId("org.pacos");
        plugin.setArtifactName("test");
        plugin.setVersion(version);
        return plugin;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAsync
    static class AsyncConfiguration {

        @Bean("pluginContextExecutor")
        ThreadPoolTaskExecutor pluginContextExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(1);
            executor.setMaxPoolSize(1);
            executor.setQueueCapacity(20);
            executor.setThreadNamePrefix("PluginLifecycleTest-");
            return executor;
        }
    }
}

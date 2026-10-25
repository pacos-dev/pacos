package org.pacos.core.component.plugin.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class PluginInfoLoaderTest {

    @Test
    void whenLoadPluginDetailsThenTriggerExecutorTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        PluginInfoLoaderTest.MyListener listener = new PluginInfoLoaderTest.MyListener(latch);

        try (MockedStatic<AppRepository> repositoryMock = mockStatic(AppRepository.class)) {
            repositoryMock.when(AppRepository::pluginRepo).thenReturn(null);
            //when
            PluginInfoLoader.refreshPluginList(listener);
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Task executor still works");
    }

    @Test
    void whenProcessIsKilledThenListenerIsNotTriggered() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        PluginInfoLoaderTest.MyListener listener = new PluginInfoLoaderTest.MyListener(latch);

        try (MockedStatic<AppRepository> repositoryMock = mockStatic(AppRepository.class)) {
            repositoryMock.when(AppRepository::pluginRepo).thenReturn(null);
            //when
            PluginInfoLoader.refreshPluginList(listener);
            PluginInfoLoader.killProcess();
        }
        assertFalse(latch.await(1, TimeUnit.SECONDS), "Task executor was triggered");
    }

    @Test
    void whenTaskIsFinishedThenTriggerListener() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        PluginInfoLoaderTest.MyListener listener = new PluginInfoLoaderTest.MyListener(latch);
        List<Plugin> configuration = new ArrayList<>();
        try (MockedStatic<RepositoryClient> clientMock = mockStatic(RepositoryClient.class)) {
            clientMock.when(() -> RepositoryClient.loadPluginList(any())).thenReturn(configuration);

            //when
            List<Plugin> received = PluginInfoLoader.refreshPluginListTask(null, listener);
            //then
            assertEquals(configuration, received);
            assertTrue(latch.await(1, TimeUnit.SECONDS), "Listener was not triggered");
        }

    }

    @Test
    void whenRefreshPluginListTaskThenUseProvidedRepository() {
        AppRepository repository = new AppRepository("partner", "https://repo.example.com/maven");
        List<Plugin> plugins = List.of(new Plugin("org.example", "sample", "Sample", "1.0.0", "", ""));
        AtomicReference<List<Plugin>> loaded = new AtomicReference<>();

        try (MockedStatic<RepositoryClient> clientMock = mockStatic(RepositoryClient.class)) {
            clientMock.when(() -> RepositoryClient.loadPluginList(repository)).thenReturn(plugins);

            List<Plugin> received = PluginInfoLoader.refreshPluginListTask(repository, loaded::set);

            assertEquals(plugins, received);
            assertEquals(plugins, loaded.get());
            clientMock.verify(() -> RepositoryClient.loadPluginList(repository));
        }
    }

    @Test
    void whenRefreshPluginListTaskFailsThenNotifyListenerWithNull() {
        AppRepository repository = new AppRepository("partner", "https://repo.example.com/maven");
        AtomicReference<List<Plugin>> loaded = new AtomicReference<>();

        try (MockedStatic<RepositoryClient> clientMock = mockStatic(RepositoryClient.class)) {
            clientMock.when(() -> RepositoryClient.loadPluginList(repository))
                    .thenThrow(new IllegalStateException("repository unavailable"));

            List<Plugin> received = PluginInfoLoader.refreshPluginListTask(repository, loaded::set);

            assertTrue(received.isEmpty());
            assertNull(loaded.get());
        }
    }

    static class MyListener implements PluginRefreshListener {

        private final CountDownLatch latch;

        public MyListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void listLoaded(List<Plugin> pluginList) {
            latch.countDown();
        }
    }
}
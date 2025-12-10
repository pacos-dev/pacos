package org.pacos.core.component.plugin.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
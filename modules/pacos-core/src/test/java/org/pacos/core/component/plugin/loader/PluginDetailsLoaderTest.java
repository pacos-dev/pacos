package org.pacos.core.component.plugin.loader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.PluginInfo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class PluginDetailsLoaderTest {

    @Test
    void whenLoadPluginDetailsThenTriggerExecutorTask() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        MyListener listener = new MyListener(latch);
        AppArtifact artifact = new AppArtifact("org", "test", "1.0");

        try (MockedStatic<AppRepository> repositoryMock = mockStatic(AppRepository.class)) {
            repositoryMock.when(AppRepository::pluginRepo).thenReturn(null);
            //when
            PluginDetailsLoader.loadPluginDetails(artifact, listener);
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Task executor still works");
    }

    @Test
    void whenProcessIsKilledThenListenerIsNotTriggered() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        MyListener listener = new MyListener(latch);
        AppArtifact artifact = new AppArtifact("org", "test", "1.0");

        try (MockedStatic<AppRepository> repositoryMock = mockStatic(AppRepository.class)) {
            repositoryMock.when(AppRepository::pluginRepo).thenReturn(null);
            //when
            PluginDetailsLoader.loadPluginDetails(artifact, listener);
            PluginDetailsLoader.killProcess();
        }
        assertFalse(latch.await(1, TimeUnit.SECONDS), "Task executor was triggered");
    }

    static class MyListener implements PluginDetailsRefreshListener {

        private final CountDownLatch latch;

        public MyListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void refreshed(PluginInfo pluginInfoDTO) {
            latch.countDown();
        }
    }
}
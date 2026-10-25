package org.pacos.core.component.plugin.loader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.data.AppRepositoryArtifact;
import org.pacos.config.repository.info.PluginInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

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

    @Test
    void whenLoadPluginDetailsTaskThenUseProvidedRepository() {
        AppArtifact artifact = new AppArtifact("org.example", "sample", "1.0.0");
        AppRepository repository = new AppRepository("partner", "https://repo.example.com/maven");
        PluginInfo expected = mock(PluginInfo.class);
        AtomicReference<AppRepositoryArtifact> requestedArtifact = new AtomicReference<>();
        AtomicReference<PluginInfo> loaded = new AtomicReference<>();

        try (MockedStatic<RepositoryClient> clientMock = mockStatic(RepositoryClient.class)) {
            clientMock.when(() -> RepositoryClient.loadInfo(any(AppRepositoryArtifact.class)))
                    .thenAnswer(invocation -> {
                        requestedArtifact.set(invocation.getArgument(0));
                        return expected;
                    });

            PluginInfo received = PluginDetailsLoader.loadPluginDetailTask(artifact, repository, loaded::set);

            assertSame(expected, received);
            assertSame(expected, loaded.get());
            assertEquals(new AppRepositoryArtifact(artifact, repository), requestedArtifact.get());
        }
    }

    @Test
    void whenLoadPluginDetailsTaskFailsThenNotifyListenerWithNull() {
        AppArtifact artifact = new AppArtifact("org.example", "sample", "1.0.0");
        AppRepository repository = new AppRepository("partner", "https://repo.example.com/maven");
        AtomicReference<PluginInfo> loaded = new AtomicReference<>();

        try (MockedStatic<RepositoryClient> clientMock = mockStatic(RepositoryClient.class)) {
            clientMock.when(() -> RepositoryClient.loadInfo(any(AppRepositoryArtifact.class)))
                    .thenThrow(new IllegalStateException("repository unavailable"));

            PluginInfo received = PluginDetailsLoader.loadPluginDetailTask(artifact, repository, loaded::set);

            assertNull(received);
            assertNull(loaded.get());
        }
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
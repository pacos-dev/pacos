package org.pacos.core.component.plugin.loader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
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
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

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
    void whenRequestOwnerIsCancelledThenListenerIsNotTriggered() throws InterruptedException {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        server.start();
        Object requestOwner = new Object();
        CountDownLatch latch = new CountDownLatch(1);
        AppRepository repository = new AppRepository("test-repository",
                "http://localhost:" + server.port() + "/repository");
        AppArtifact artifact = new AppArtifact("org.example", "sample", "1.0.0");

        try {
            server.stubFor(get("/repository/org/example/sample/1.0.0/sample-1.0.0.xml")
                    .willReturn(aResponse().withStatus(404).withFixedDelay(2000)));

            PluginDetailsLoader.loadPluginDetails(artifact, repository, requestOwner, info -> latch.countDown());
            PluginDetailsLoader.killProcess(requestOwner);

            assertFalse(latch.await(1, TimeUnit.SECONDS), "Cancelled request must not notify its listener");
        } finally {
            PluginDetailsLoader.killProcess(requestOwner);
            server.stop();
        }
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

    @Test
    void whenLoadingDetailsForDifferentOwnersThenNotifyBothListeners() throws InterruptedException {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        server.start();
        Object firstOwner = new Object();
        Object secondOwner = new Object();
        CountDownLatch callbacks = new CountDownLatch(2);
        AtomicInteger callbackCount = new AtomicInteger();
        AppRepository repository = new AppRepository("test-repository",
                "http://localhost:" + server.port() + "/repository");
        AppArtifact artifact = new AppArtifact("org.example", "sample", "1.0.0");
        PluginDetailsRefreshListener listener = pluginInfo -> {
            callbackCount.incrementAndGet();
            callbacks.countDown();
        };

        try {
            server.stubFor(get("/repository/org/example/sample/1.0.0/sample-1.0.0.xml")
                    .willReturn(aResponse().withStatus(404).withFixedDelay(500)));

            PluginDetailsLoader.loadPluginDetails(artifact, repository, firstOwner, listener);
            PluginDetailsLoader.loadPluginDetails(artifact, repository, secondOwner, listener);

            assertTrue(callbacks.await(5, TimeUnit.SECONDS));
            assertEquals(2, callbackCount.get());
        } finally {
            PluginDetailsLoader.killProcess(firstOwner);
            PluginDetailsLoader.killProcess(secondOwner);
            server.stop();
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
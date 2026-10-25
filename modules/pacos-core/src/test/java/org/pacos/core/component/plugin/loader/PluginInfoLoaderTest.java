package org.pacos.core.component.plugin.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.info.Plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.mockito.Mockito.mockStatic;

class PluginInfoLoaderTest {

    @Test
    void whenRefreshPluginListThenTriggerExecutorTask() throws InterruptedException {
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
        AppRepository repository = new AppRepository("test-repository", "https://repo.example.com/maven");
        try (MockedStatic<RepositoryClient> clientMock = mockStatic(RepositoryClient.class)) {
            clientMock.when(() -> RepositoryClient.loadPluginList(repository)).thenReturn(configuration);

            //when
            List<Plugin> received = PluginInfoLoader.refreshPluginListTask(repository, listener);
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

    @Test
    void whenLoadingPluginListsForDifferentOwnersThenNotifyBothListeners() throws InterruptedException {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        server.start();
        Object firstOwner = new Object();
        Object secondOwner = new Object();
        CountDownLatch callbacks = new CountDownLatch(2);
        AtomicInteger callbackCount = new AtomicInteger();
        AppRepository repository = new AppRepository("test-repository",
                "http://localhost:" + server.port() + "/repository/pacos-maven-repo");
        PluginRefreshListener listener = plugins -> {
            callbackCount.incrementAndGet();
            callbacks.countDown();
        };

        try {
            server.stubFor(get("/repository/pacos-maven-repo/org/pacos/info/plugin/maven-metadata.xml")
                    .willReturn(aResponse().withStatus(404).withFixedDelay(500)));

            PluginInfoLoader.refreshPluginList(repository, firstOwner, listener);
            PluginInfoLoader.refreshPluginList(repository, secondOwner, listener);

            assertTrue(callbacks.await(5, TimeUnit.SECONDS));
            assertEquals(2, callbackCount.get());
        } finally {
            PluginInfoLoader.killProcess(firstOwner);
            PluginInfoLoader.killProcess(secondOwner);
            server.stop();
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
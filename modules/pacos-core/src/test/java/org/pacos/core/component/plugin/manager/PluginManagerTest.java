package org.pacos.core.component.plugin.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.service.PluginService;

class PluginManagerTest {

    private PluginService pluginService;
    private PluginState pluginState;
    private PluginLifecycle pluginLifecycle;
    private PluginManager manager;

    @BeforeEach
    void setUp() {
        pluginService = mock(PluginService.class);
        pluginState = spy(new PluginState());
        pluginLifecycle = mock(PluginLifecycle.class);
        manager = new PluginManager(pluginService, pluginState, pluginLifecycle);
    }

    @Test
    void whenCallInitializePluginsThenInitializeResourcesAndCatalogBeforeStartingEnabledPluginsSequentially() {
        PluginDTO disabledPlugin = createPlugin("1.0");
        PluginDTO firstEnabledPlugin = createPlugin("1.1");
        PluginDTO secondEnabledPlugin = createPlugin("1.2");
        when(pluginService.findNotRemovedPlugin())
                .thenReturn(List.of(disabledPlugin, firstEnabledPlugin, secondEnabledPlugin));
        when(pluginService.findEnabledPlugin()).thenReturn(List.of(firstEnabledPlugin, secondEnabledPlugin));
        CompletableFuture<Boolean> firstStart = spy(CompletableFuture.completedFuture(false));
        CompletableFuture<Boolean> secondStart = spy(CompletableFuture.completedFuture(true));
        when(pluginLifecycle.startPlugin(firstEnabledPlugin)).thenReturn(firstStart);
        when(pluginLifecycle.startPlugin(secondEnabledPlugin)).thenReturn(secondStart);

        manager.initializePluginsOnApplicationReadyEvent();

        InOrder order = inOrder(pluginLifecycle, pluginService, pluginState, firstStart, secondStart);
        order.verify(pluginLifecycle).initializeResources();
        order.verify(pluginService).findNotRemovedPlugin();
        order.verify(pluginState).addPlugin(disabledPlugin);
        order.verify(pluginState).addPlugin(firstEnabledPlugin);
        order.verify(pluginState).addPlugin(secondEnabledPlugin);
        order.verify(pluginService).findEnabledPlugin();
        order.verify(pluginLifecycle).startPlugin(firstEnabledPlugin);
        order.verify(firstStart).join();
        order.verify(pluginLifecycle).startPlugin(secondEnabledPlugin);
        order.verify(secondStart).join();
        verifyNoMoreInteractions(pluginLifecycle);
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(disabledPlugin));
        assertEquals(Set.of(disabledPlugin, firstEnabledPlugin, secondEnabledPlugin), pluginState.getPlugins());
    }

    @Test
    void whenCallAddPluginThenAddPluginToCatalogWithOffState() {
        PluginDTO plugin = createPlugin("1.0");

        manager.addPlugin(plugin);

        verify(pluginState).addPlugin(plugin);
        assertEquals(PluginStatusEnum.OFF, pluginState.getState(plugin));
        assertEquals(Set.of(plugin), pluginState.getPlugins());
        verifyNoInteractions(pluginLifecycle);
    }

    @Test
    void whenCallAddPluginAgainThenPreserveStateAndSingleCatalogEntry() {
        PluginDTO plugin = createPlugin("1.0");
        manager.addPlugin(plugin);
        pluginState.setState(plugin, PluginStatusEnum.ON);
        PluginDTO equivalentPlugin = createPlugin("1.0");

        manager.addPlugin(equivalentPlugin);

        verify(pluginState).addPlugin(same(equivalentPlugin));
        assertEquals(PluginStatusEnum.ON, pluginState.getState(equivalentPlugin));
        assertEquals(Set.of(plugin), pluginState.getPlugins());
        verifyNoInteractions(pluginLifecycle);
    }

    @Test
    void whenCallStartPluginThenReturnUnchangedLifecycleFuture() {
        PluginDTO plugin = createPlugin("1.0");
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        when(pluginLifecycle.startPlugin(plugin)).thenReturn(future);

        assertSame(future, manager.startPlugin(plugin));

        verify(pluginLifecycle).startPlugin(plugin);
        verifyNoInteractions(pluginState);
    }

    @Test
    void whenCallStopPluginThenReturnUnchangedLifecycleFuture() {
        PluginDTO plugin = createPlugin("1.0");
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        when(pluginLifecycle.stopPlugin(plugin)).thenReturn(future);

        assertSame(future, manager.stopPlugin(plugin));

        verify(pluginLifecycle).stopPlugin(plugin);
        verifyNoInteractions(pluginState);
    }

    @Test
    void whenCallRemovePluginThenRemoveFromCatalogOnlyThroughLifecycleCallback() {
        PluginDTO plugin = createPlugin("1.0");
        manager.addPlugin(plugin);
        pluginState.setState(plugin, PluginStatusEnum.ON);

        manager.removePlugin(plugin);

        ArgumentCaptor<Runnable> callback = ArgumentCaptor.forClass(Runnable.class);
        verify(pluginLifecycle).removePlugin(eq(plugin), callback.capture());
        assertEquals(PluginStatusEnum.ON, pluginState.getState(plugin));
        assertEquals(Set.of(plugin), pluginState.getPlugins());

        callback.getValue().run();

        assertNull(pluginState.getState(plugin));
        assertTrue(pluginState.getPlugins().isEmpty());
    }

    @Test
    void whenCallRemovePluginAndCleanupFailsAfterCallbackThenPropagateFailureAndKeepCatalogEmpty() {
        PluginDTO plugin = createPlugin("1.0");
        manager.addPlugin(plugin);
        IllegalStateException failure = new IllegalStateException("cleanup failed");
        doAnswer(invocation -> {
            invocation.getArgument(1, Runnable.class).run();
            throw failure;
        }).when(pluginLifecycle).removePlugin(eq(plugin), any(Runnable.class));

        assertSame(failure, assertThrows(IllegalStateException.class, () -> manager.removePlugin(plugin)));

        assertNull(pluginState.getState(plugin));
        assertTrue(pluginState.getPlugins().isEmpty());
    }

    @Test
    void whenCallRemovePluginAndLifecycleFailsBeforeCallbackThenPropagateFailureWithoutRemovingCatalogEntry() {
        PluginDTO plugin = createPlugin("1.0");
        manager.addPlugin(plugin);
        IllegalStateException failure = new IllegalStateException("removal failed");
        doThrow(failure).when(pluginLifecycle).removePlugin(eq(plugin), any(Runnable.class));

        assertSame(failure, assertThrows(IllegalStateException.class, () -> manager.removePlugin(plugin)));

        assertEquals(PluginStatusEnum.OFF, pluginState.getState(plugin));
        assertEquals(Set.of(plugin), pluginState.getPlugins());
    }

    private PluginDTO createPlugin(String version) {
        PluginDTO plugin = new PluginDTO();
        plugin.setArtifactName("test");
        plugin.setGroupId("org.pacos");
        plugin.setVersion(version);
        return plugin;
    }
}

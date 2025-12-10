package org.pacos.core.component.plugin.event;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pacos.core.component.plugin.loader.PluginInfoLoader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class RefreshAvailablePluginEventTest {

    @Test
    void whenFireEventThenNoException() {
        try (MockedStatic<PluginInfoLoader> mock = mockStatic(PluginInfoLoader.class)) {
            mock.when(() -> PluginInfoLoader.refreshPluginList(any()))
                    .thenAnswer(inv -> null);
        }
        //when
        assertDoesNotThrow(() -> RefreshAvailablePluginEvent.fireEvent(listener -> {
        }));
    }
}
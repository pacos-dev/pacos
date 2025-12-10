package org.pacos.core.component.variable.service.provider;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class VariableProviderHandlerTest {

    private final UserVariableProviderImpl userVariableProviderMock = mock(UserVariableProviderImpl.class);
    private final UserGlobalVariableProviderImpl userGlobalVariableProviderMock = mock(UserGlobalVariableProviderImpl.class);
    private final SystemVariableProviderImpl systemVariableProviderMock = mock(SystemVariableProviderImpl.class);

    private final VariableProviderHandler handler = new VariableProviderHandler(
            List.of(userVariableProviderMock,
                    userGlobalVariableProviderMock,
                    systemVariableProviderMock)
    );

    @Test
    void whenGetProvidersThenReturnAllProviders() {
        List<VariableProvider> providers = handler.getProviders();

        assertNotNull(providers);
        assertEquals(3, providers.size());
        assertTrue(providers.contains(userVariableProviderMock));
        assertTrue(providers.contains(userGlobalVariableProviderMock));
        assertTrue(providers.contains(systemVariableProviderMock));
    }
}

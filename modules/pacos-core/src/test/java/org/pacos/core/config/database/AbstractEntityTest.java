package org.pacos.core.config.database;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.Test;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AbstractEntityTest {

    @Test
    void whenPrePersistWithoutVaadinSessionThenAuditFieldsUseSystemUser() {
        AbstractEntity entity = new TestEntity();

        entity.prePersist();

        assertNotNull(entity.getCreationTime());
        assertNotNull(entity.getUpdateTime());
        assertEquals(0, entity.getCreatedBy());
        assertEquals(0, entity.getUpdatedBy());
    }

    @Test
    void whenPreUpdateWithoutVaadinSessionThenUpdatedByUsesSystemUser() {
        AbstractEntity entity = new TestEntity();

        entity.preUpdate();

        assertNotNull(entity.getUpdateTime());
        assertEquals(0, entity.getUpdatedBy());
    }

    @Test
    void whenPrePersistWithSessionAndUserThenAuditFieldsUseCurrentUser() {
        VaadinSession session = mock(VaadinSession.class);
        UserDTO user = mock(UserDTO.class);
        UserSession userSession = new UserSession(user);
        when(user.getId()).thenReturn(42);
        when(session.getAttribute(UserSession.class)).thenReturn(userSession);

        try (var vaadin = mockStatic(VaadinSession.class);
             var ui = mockStatic(UI.class)) {
            ui.when(UI::getCurrent).thenReturn(mock(UI.class));
            vaadin.when(VaadinSession::getCurrent).thenReturn(session);
            entityCurrentSession(userSession, () -> {
                AbstractEntity entity = new TestEntity();
                entity.prePersist();
                assertEquals(42, entity.getCreatedBy());
                assertEquals(42, entity.getUpdatedBy());
                assertNotNull(entity.getCreationTime());
                assertNotNull(entity.getUpdateTime());
            });
        }
    }

    @Test
    void whenPreUpdateWithSessionAndUserThenUpdatedByUsesCurrentUser() {
        VaadinSession session = mock(VaadinSession.class);
        UserDTO user = mock(UserDTO.class);
        UserSession userSession = new UserSession(user);
        when(user.getId()).thenReturn(77);
        when(session.getAttribute(UserSession.class)).thenReturn(userSession);

        try (var vaadin = mockStatic(VaadinSession.class);
             var ui = mockStatic(UI.class)) {
            ui.when(UI::getCurrent).thenReturn(mock(UI.class));
            vaadin.when(VaadinSession::getCurrent).thenReturn(session);
            entityCurrentSession(userSession, () -> {
                AbstractEntity entity = new TestEntity();
                entity.preUpdate();
                assertNotNull(entity.getUpdateTime());
                assertEquals(77, entity.getUpdatedBy());
            });
        }
    }

    private static void entityCurrentSession(UserSession userSession, Runnable test) {
        test.run();
    }

    private static class TestEntity extends AbstractEntity {
    }
}

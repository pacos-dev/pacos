package org.pacos.core.component.settings.view.tab;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.pacos.base.security.Permission;
import org.pacos.base.session.AccessDecision;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.security.dto.PermissionDetailConfig;
import org.pacos.core.component.security.service.PermissionDefaultService;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DefaultPermissionsTabLayoutTest {

    @Mock
    private PermissionDefaultService defaultService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
       VaadinMock.mockSystem();
    }

    @Test
    void whenLayoutCreatedThenContainsInfoBoxAndGridWithExpectedColumns() {
        DefaultPermissionsTabLayout layout = new DefaultPermissionsTabLayout(defaultService);

        assertTrue(layout.getChildren().anyMatch(InfoBox.class::isInstance));

        Grid<PermissionDetailConfig> grid = extractGrid(layout);
        List<String> headers = grid.getColumns().stream()
                .map(Grid.Column::getHeaderText)
                .collect(Collectors.toList());

        assertEquals(List.of("Category", "Key", "Label", "Description", "Allowed"), headers);
    }

    @Test
    void whenAllowedCheckboxToggledThenServiceUpdatedAndNotificationShown() {
        PermissionDetailConfig permission = new PermissionDetailConfig(createPermission(),
                AccessDecision.ALLOW);
        DefaultPermissionsTabLayout layout = new DefaultPermissionsTabLayout(defaultService);

        Grid<PermissionDetailConfig> grid = extractGrid(layout);

        ComponentRenderer<Checkbox, PermissionDetailConfig> renderer =
                (ComponentRenderer<Checkbox, PermissionDetailConfig>) grid.getColumns().get(4).getRenderer();

        Checkbox checkbox = renderer.createComponent(permission);

        try (MockedStatic<NotificationUtils> notifications = mockStatic(NotificationUtils.class)) {
            notifications.when(() -> NotificationUtils.success(anyString())).thenReturn(mock(Notification.class));

            checkbox.setValue(false);

            verify(defaultService).updateConfiguration(permission, false);
            notifications.verify(() -> NotificationUtils.success("Configuration was updated"));
        }
    }

    @SuppressWarnings("unchecked")
    private static Grid<PermissionDetailConfig> extractGrid(DefaultPermissionsTabLayout layout) {
        return layout.getChildren()
                .filter(Grid.class::isInstance)
                .map(component -> (Grid<PermissionDetailConfig>) component)
                .findFirst()
                .orElseThrow();
    }

    private static Permission createPermission() {
        return new Permission() {
            @Override
            public String getKey() {
                return "key";
            }

            @Override
            public String getLabel() {
                return "label";
            }

            @Override
            public String getCategory() {
                return "category";
            }

            @Override
            public String getDescription() {
                return "description";
            }
        };
    }

    @Test
    void whenGetSearchIndexThenReturnNotEmptyString(){
        assertNotNull(DefaultPermissionsTabLayout.getSearchIndex());
    }
}


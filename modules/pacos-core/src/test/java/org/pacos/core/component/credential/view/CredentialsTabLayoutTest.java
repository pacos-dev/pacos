package org.pacos.core.component.credential.view;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;
import org.pacos.core.component.credential.service.CredentialVisibility;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.base.window.event.OnConfirmEvent;
import org.pacos.core.component.credential.service.CredentialTypeRegistry;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.security.service.RoleService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CredentialsTabLayoutTest {

    private CredentialVaultService credentialService;
    private RoleService roleService;
    private PluginProxy pluginProxy;
    private UserSession userSession;
    private org.pacos.base.event.UISystem uiSystem;

    @BeforeEach
    void setUp() {
        uiSystem = VaadinMock.mockSystem();
        credentialService = mock(CredentialVaultService.class);
        roleService = mock(RoleService.class);
        pluginProxy = mock(PluginProxy.class);
        userSession = mock(UserSession.class);
        when(userSession.getUserId()).thenReturn(7);
        when(userSession.getUISystem()).thenReturn(uiSystem);
        when(credentialService.getAvailableTypes()).thenReturn(List.of());
        when(roleService.loadAllRoles()).thenReturn(List.of());
        when(credentialService.findAccess(any(), any())).thenReturn(List.of());
        PluginState pluginState = mock(PluginState.class);
        when(pluginProxy.getPluginState()).thenReturn(pluginState);
        when(pluginState.getPlugins()).thenReturn(Set.of(plugin("ApiNity", "apinity")));
        when(credentialService.findAvailable(eq(new CredentialQuery(null, "")), eq(CredentialAccessContext.forUser(7))))
                .thenReturn(List.of());
    }

    @Test
    void whenInitializeLayoutThenLoadAvailableCredentials() {
        assertDoesNotThrow(() -> new CredentialsTabLayout(
                credentialService, roleService, pluginProxy, userSession, 10 * 1024 * 1024L));

        verify(credentialService).findAvailable(
                new CredentialQuery(null, ""), CredentialAccessContext.forUser(7));
    }

    @Test
    void whenSearchTermChangesThenReloadCredentialsWithSearchTerm() throws ReflectiveOperationException {
        CredentialsTabLayout layout = createLayout();
        field(layout, "searchField", TextField.class).setValue("token");

        verify(credentialService).findAvailable(
                new CredentialQuery(null, "token"), CredentialAccessContext.forUser(7));
    }

    @Test
    void whenCredentialTypeChangesThenReloadCredentialsWithType() throws ReflectiveOperationException {
        CredentialsTabLayout layout = createLayout();
        CredentialTypeDefinition type = new CredentialTypeRegistry().getRequired("bearer-token");
        field(layout, "typeFilter", ComboBox.class).setValue(type);

        verify(credentialService).findAvailable(
                new CredentialQuery("bearer-token", ""), CredentialAccessContext.forUser(7));
    }

    @Test
    void whenCredentialIsNotManageableThenDoNotRenderManagementActions() {
        CredentialsTabLayout layout = createLayout();
        HorizontalLayout actions = layout.createActions(descriptor(false));

        assertTrue(actions.getChildren().findAny().isEmpty());
    }

    @Test
    void whenCredentialIsManageableThenRenderEditAccessAndDeleteActions() {
        CredentialsTabLayout layout = createLayout();
        HorizontalLayout actions = layout.createActions(descriptor(true));

        List<String> labels = actions.getChildren()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .map(Button::getText)
                .toList();

        assertEquals(List.of("Edit", "Access", "Delete"), labels);
    }

    @Test
    void whenGuestOpensCredentialsTabThenDoNotShowCreateButton() throws ReflectiveOperationException {
        when(userSession.getUserId()).thenReturn(UserDTO.GUEST_ID);
        CredentialsTabLayout layout = createLayout();
        HorizontalLayout toolbar = field(layout, "toolbar", HorizontalLayout.class);

        assertFalse(toolbar.getChildren()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .anyMatch(button -> button.getText().equals("Create credential")));
    }

    @Test
    void whenCreateDeleteConfirmationThenUseConfirmationWindowConfig() throws ReflectiveOperationException {
        CredentialsTabLayout layout = createLayout();
        CredentialAdminDescriptor descriptor = descriptor(true);

        ConfirmationWindowConfig config = layout.createDeleteConfirmationConfig(descriptor);

        assertEquals("Delete credential", config.title());
        assertEquals(440, windowStateValue(config, "width"));
        assertEquals(180, windowStateValue(config, "height"));
        assertTrue((Boolean) windowStateValueObject(config, "warning"));
        OnConfirmEvent confirmEvent = (OnConfirmEvent) windowStateValueObject(config, "confirmEvent");
        assertTrue(confirmEvent.confirm());
        verify(credentialService).delete(new CredentialReference(descriptor.id()),
                CredentialAccessContext.forUser(7));
    }

    @Test
    void whenDeleteFailsThenKeepConfirmationOpen() throws ReflectiveOperationException {
        CredentialsTabLayout layout = createLayout();
        CredentialAdminDescriptor descriptor = descriptor(true);
        doThrow(new IllegalStateException("delete failed"))
                .when(credentialService).delete(new CredentialReference(descriptor.id()), CredentialAccessContext.forUser(7));

        ConfirmationWindowConfig config = layout.createDeleteConfirmationConfig(descriptor);
        OnConfirmEvent confirmEvent = (OnConfirmEvent) windowStateValueObject(config, "confirmEvent");

        assertFalse(confirmEvent.confirm());
    }

    @Test
    void whenRefreshFailsThenClearCredentialGrid() throws ReflectiveOperationException {
        CredentialsTabLayout layout = createLayout();
        when(credentialService.findAvailable(
                new CredentialQuery(null, ""), CredentialAccessContext.forUser(7)))
                .thenThrow(new IllegalStateException("database unavailable"));

        layout.refresh();

        Grid<CredentialAdminDescriptor> grid = field(layout, "grid", Grid.class);
        assertEquals(0, grid.getDataProvider().size(new Query<>()));
    }

    @Test
    void whenCreateButtonIsClickedThenOpenCreateCredentialModal() throws ReflectiveOperationException {
        when(credentialService.getAvailableTypes()).thenReturn(
                List.of(new CredentialTypeRegistry().getRequired("bearer-token")));
        CredentialsTabLayout layout = createLayout();
        HorizontalLayout toolbar = field(layout, "toolbar", HorizontalLayout.class);
        Button create = toolbar.getChildren()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getText().equals("Create credential"))
                .findFirst()
                .orElseThrow();

        create.click();

        org.mockito.ArgumentCaptor<ModalWindowConfig> captor =
                org.mockito.ArgumentCaptor.forClass(ModalWindowConfig.class);
        verify(uiSystem.getWindowManager()).showModalWindow(captor.capture());
        assertEquals("Create credential", captor.getValue().title());
    }

    @Test
    void whenEditActionIsClickedThenOpenEditCredentialModal() {
        when(credentialService.getAvailableTypes()).thenReturn(
                List.of(new CredentialTypeRegistry().getRequired("bearer-token")));
        when(credentialService.findNonSensitiveValues(any(), any())).thenReturn(java.util.Map.of());
        CredentialsTabLayout layout = createLayout();
        Button edit = layout.createActions(descriptor(true)).getChildren()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getText().equals("Edit"))
                .findFirst()
                .orElseThrow();

        edit.click();

        org.mockito.ArgumentCaptor<ModalWindowConfig> captor =
                org.mockito.ArgumentCaptor.forClass(ModalWindowConfig.class);
        verify(uiSystem.getWindowManager()).showModalWindow(captor.capture());
        assertEquals("Edit credential", captor.getValue().title());
    }

    @Test
    void whenAccessActionIsClickedThenOpenCredentialAccessModal() {
        CredentialsTabLayout layout = createLayout();
        Button access = layout.createActions(descriptor(true)).getChildren()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getText().equals("Access"))
                .findFirst()
                .orElseThrow();

        access.click();

        verify(uiSystem.getWindowManager()).showModalWindow(any(ModalWindowConfig.class));
    }

    @Test
    void whenDeleteActionIsClickedThenOpenConfirmationModal() {
        CredentialsTabLayout layout = createLayout();
        Button delete = layout.createActions(descriptor(true)).getChildren()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getText().equals("Delete"))
                .findFirst()
                .orElseThrow();

        delete.click();

        verify(uiSystem.getWindowManager()).showModalWindow(any(ConfirmationWindowConfig.class));
        assertTrue(delete.getThemeNames().contains("error_l"));
    }

    @Test
    void whenGetSearchIndexCalledThenReturnNonEmptyIndex() {
        assertFalse(CredentialsTabLayout.getSearchIndex().isBlank());
    }

    private CredentialsTabLayout createLayout() {
        return new CredentialsTabLayout(
                credentialService, roleService, pluginProxy, userSession, 10 * 1024 * 1024L);
    }

    private static PluginDTO plugin(String name, String artifactName) {
        PluginDTO plugin = new PluginDTO();
        plugin.setName(name);
        plugin.setArtifactName(artifactName);
        return plugin;
    }

    private static CredentialAdminDescriptor descriptor(boolean manageable) {
        return new CredentialAdminDescriptor(
                UUID.randomUUID(), "Production API", null, "bearer-token", 7, CredentialVisibility.PRIVATE,
                Instant.now(), Instant.now(), Set.of("token"), Set.of(), manageable);
    }

    private static int windowStateValue(ConfirmationWindowConfig config, String name)
            throws ReflectiveOperationException {
        java.lang.reflect.Field field = config.getWindowState().getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(config.getWindowState());
    }

    private static Object windowStateValueObject(ConfirmationWindowConfig config, String name)
            throws ReflectiveOperationException {
        java.lang.reflect.Field field = config.getWindowState().getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(config.getWindowState());
    }

    private static <T> T field(Object target, String name, Class<T> type) throws ReflectiveOperationException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}

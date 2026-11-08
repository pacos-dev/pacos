package org.pacos.core.component.credential.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.credential.CredentialReference;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAccessDescriptor;
import org.pacos.core.component.credential.service.CredentialAccessGrant;
import org.pacos.core.component.credential.service.CredentialAclPermission;
import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.core.component.credential.service.CredentialPrincipalType;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.core.component.credential.service.CredentialVisibility;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.RoleService;

import com.vaadin.flow.component.combobox.ComboBox;

class CredentialAccessDialogTest {

    private CredentialVaultService credentialService;
    private RoleService roleService;
    private PluginProxy pluginProxy;
    private PluginState pluginState;
    private CredentialAdminDescriptor descriptor;
    private CredentialAccessContext accessContext;

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
        credentialService = mock(CredentialVaultService.class);
        roleService = mock(RoleService.class);
        pluginProxy = mock(PluginProxy.class);
        pluginState = mock(PluginState.class);
        when(pluginProxy.getPluginState()).thenReturn(pluginState);
        when(pluginState.getPlugins()).thenReturn(Set.of(plugin("ApiNity", "apinity")));
        when(roleService.loadAllRoles()).thenReturn(List.of(group(1, "ROLE_GUEST"), group(3, "DEVELOPERS")));
        when(credentialService.findAccess(any(), any())).thenReturn(List.of());
        descriptor = new CredentialAdminDescriptor(
                UUID.randomUUID(),
                "Production API",
                null,
                "bearer-token",
                7,
                CredentialVisibility.PRIVATE,
                Instant.now(),
                Instant.now(),
                Set.of("token"),
                Set.of(),
                false);
        accessContext = CredentialAccessContext.forUser(7);
    }

    @Test
    void whenInitializeAccessDialogThenLoadCredentialGrants() {
        assertDoesNotThrow(() -> new CredentialAccessDialog(
                credentialService, roleService, pluginProxy, accessContext, descriptor));

        verify(credentialService).findAccess(any(), any());
        verify(roleService).loadAllRoles();
    }

    @Test
    void whenAccessDialogCreatesWindowConfigThenUseFixedSizeModalWindow() throws ReflectiveOperationException {
        ModalWindowConfig config = new CredentialAccessDialog(
                credentialService, roleService, pluginProxy, accessContext, descriptor).toWindowConfig(descriptor.name());

        assertEquals("Access — Production API", config.title());
        assertEquals(900, windowStateValue(config, "width"));
        assertEquals(600, windowStateValue(config, "height"));
    }

    @Test
    void whenGrantAlreadyConfiguredThenKeepSelectionAndShowFieldError() throws ReflectiveOperationException {
        when(credentialService.findAccess(any(), any())).thenReturn(List.of(
                new CredentialAccessDescriptor(CredentialPrincipalType.GROUP, "3", CredentialAclPermission.ACCESS)));
        CredentialAccessDialog dialog = new CredentialAccessDialog(
                credentialService, roleService, pluginProxy, accessContext, descriptor);
        ComboBox<RoleDTO> group = field(dialog, "group");
        group.setValue(group.getListDataView().getItems()
                .filter(role -> role.getId() == 3)
                .findFirst()
                .orElseThrow());

        invoke(dialog, "grantAccess");

        assertEquals(3, group.getValue().getId());
        assertTrue(group.isInvalid());
        assertEquals("This configuration already exists.", group.getErrorMessage());
        group.setValue(group.getListDataView().getItems()
                .filter(role -> role.getId() == 1)
                .findFirst()
                .orElseThrow());
        assertFalse(group.isInvalid());
        org.mockito.Mockito.verify(credentialService, org.mockito.Mockito.never())
                .grantAccess(any(), any(), any());
    }

    @Test
    void whenGrantRefreshFailsThenDoNotUseStaleGrantsForDuplicateDetection() throws ReflectiveOperationException {
        when(credentialService.findAccess(any(), any()))
                .thenReturn(List.of(new CredentialAccessDescriptor(
                        CredentialPrincipalType.GROUP, "3", CredentialAclPermission.ACCESS)))
                .thenThrow(new IllegalStateException("temporary failure"));
        CredentialAccessDialog dialog = new CredentialAccessDialog(
                credentialService, roleService, pluginProxy, accessContext, descriptor);
        ComboBox<RoleDTO> group = field(dialog, "group");
        group.setValue(group.getListDataView().getItems()
                .filter(role -> role.getId() == 3)
                .findFirst()
                .orElseThrow());

        invoke(dialog, "refresh");
        invoke(dialog, "grantAccess");

        verify(credentialService).grantAccess(
                eq(new CredentialReference(descriptor.id())),
                eq(new CredentialAccessGrant(
                        CredentialPrincipalType.GROUP, "3", CredentialAclPermission.ACCESS)),
                eq(accessContext));
    }

    @Test
    void whenPluginIsSelectedThenGrantAccessUsingInstalledPluginArtifactName() throws ReflectiveOperationException {
        CredentialAccessDialog dialog = new CredentialAccessDialog(
                credentialService, roleService, pluginProxy, accessContext, descriptor);
        ComboBox<CredentialPrincipalType> principalType = field(dialog, "principalType");
        ComboBox<PluginDTO> plugin = field(dialog, "plugin");
        PluginDTO selectedPlugin = plugin.getListDataView().getItems().findFirst().orElseThrow();
        principalType.setValue(CredentialPrincipalType.PLUGIN);
        plugin.setValue(selectedPlugin);

        invoke(dialog, "grantAccess");

        assertEquals("apinity", selectedPlugin.getArtifactName());
        verify(credentialService).grantAccess(
                eq(new CredentialReference(descriptor.id())),
                eq(new CredentialAccessGrant(
                        CredentialPrincipalType.PLUGIN, "apinity", CredentialAclPermission.ACCESS)),
                eq(accessContext));
    }

    @Test
    void whenGuestGroupIsSelectedThenOnlyAccessPermissionIsAvailable() throws ReflectiveOperationException {
        CredentialAccessDialog dialog = new CredentialAccessDialog(
                credentialService, roleService, pluginProxy, accessContext, descriptor);
        ComboBox<RoleDTO> group = field(dialog, "group");
        ComboBox<CredentialAclPermission> permission = field(dialog, "permission");

        assertTrue(group.getListDataView().getItems().anyMatch(role -> role.getId() == 1));

        group.setValue(group.getListDataView().getItems()
                .filter(role -> role.getId() == 1)
                .findFirst()
                .orElseThrow());

        assertEquals(List.of(CredentialAclPermission.ACCESS), permission.getListDataView().getItems().toList());
    }

    private static int windowStateValue(ModalWindowConfig config, String name) throws ReflectiveOperationException {
        Field field = config.getWindowState().getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(config.getWindowState());
    }

    private static void invoke(Object target, String name) throws ReflectiveOperationException {
        java.lang.reflect.Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }

    @SuppressWarnings("unchecked")
    private static <T> T field(Object target, String name) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static PluginDTO plugin(String name, String artifactName) {
        PluginDTO plugin = new PluginDTO();
        plugin.setName(name);
        plugin.setArtifactName(artifactName);
        return plugin;
    }

    private static RoleDTO group(int id, String label) {
        RoleDTO role = new RoleDTO();
        role.setId(id);
        role.setLabel(label);
        return role;
    }
}

package org.pacos.core.component.credential.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.credential.CredentialReference;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.base.window.event.OnConfirmEvent;
import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.core.component.credential.service.CredentialCreateRequest;
import org.pacos.core.component.credential.service.CredentialFileInput;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;
import org.pacos.core.component.credential.service.CredentialTypeRegistry;
import org.pacos.core.component.credential.service.CredentialUpdateRequest;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.core.component.credential.service.CredentialVisibility;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.PasswordField;

class CredentialEditorDialogTest {

    private CredentialVaultService service;

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
        service = mock(CredentialVaultService.class);
    }

    @Test
    void whenCreateDialogForEveryCredentialTypeThenBuildDynamicFields() {
        for (CredentialTypeDefinition definition : new CredentialTypeRegistry().getAll()) {
            when(service.getAvailableTypes()).thenReturn(List.of(definition));

            assertDoesNotThrow(() -> new CredentialEditorDialog(
                    service, CredentialAccessContext.forUser(7), null, true, 10 * 1024 * 1024L, () -> { })
                    .toWindowConfig());
        }
    }

    @Test
    void whenCreateDialogThenUseFixedSizeModalWindowConfig() {
        when(service.getAvailableTypes()).thenReturn(List.of(new CredentialTypeRegistry().getRequired("bearer-token")));

        ModalWindowConfig config = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), null, true, 10 * 1024 * 1024L, () -> { })
                .toWindowConfig();

        assertEquals("Create credential", config.title());
        assertEquals(760, windowStateValue(config, "width"));
        assertEquals(720, windowStateValue(config, "height"));
        assertTrue((Boolean) windowStateObject(config, "cancellable"));
        assertTrue(windowStateObject(config, "confirmEvent") != null);
    }

    @Test
    void whenCreateCredentialThenPersistValuesAndClearSecretInput() {
        CredentialTypeDefinition definition = new CredentialTypeRegistry().getRequired("bearer-token");
        when(service.getAvailableTypes()).thenReturn(List.of(definition));
        AtomicInteger saved = new AtomicInteger();
        AtomicReference<char[]> capturedSecret = new AtomicReference<>();
        when(service.create(any(), eq(CredentialAccessContext.forUser(7)))).thenAnswer(invocation -> {
            CredentialCreateRequest request = invocation.getArgument(0);
            capturedSecret.set(((char[]) request.values().get("token")).clone());
            return null;
        });

        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), null, true, 10 * 1024 * 1024L, saved::incrementAndGet);
        dialog.getNameField().setValue("Production API");
        PasswordField secret = (PasswordField) dialog.getFieldsForm().getFieldComponent("token");
        secret.setValue("token-value");

        assertTrue(dialog.save());

        assertArrayEquals("token-value".toCharArray(), capturedSecret.get());
        assertTrue(secret.getValue().isEmpty());
        assertEquals(1, saved.get());
    }

    @Test
    void whenCreateCredentialWithoutNameThenKeepDialogOpenAndDoNotPersist() {
        when(service.getAvailableTypes()).thenReturn(List.of(new CredentialTypeRegistry().getRequired("bearer-token")));
        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), null, true, 10 * 1024 * 1024L, () -> { });

        assertFalse(dialog.save());

        verify(service, never()).create(any(), any());
    }

    @Test
    void whenEditCredentialThenPreserveWriteOnlySecretWhenNoReplacementIsEntered() {
        CredentialTypeDefinition definition = new CredentialTypeRegistry().getRequired("basic-auth");
        when(service.getAvailableTypes()).thenReturn(List.of(definition));
        when(service.findNonSensitiveValues(any(), any())).thenReturn(Map.of("username", "alice"));
        AtomicReference<CredentialUpdateRequest> capturedRequest = new AtomicReference<>();
        when(service.update(any(), any(), any())).thenAnswer(invocation -> {
            capturedRequest.set(invocation.getArgument(1));
            return null;
        });
        CredentialAdminDescriptor descriptor = new CredentialAdminDescriptor(
                UUID.randomUUID(), "Production", null, "basic-auth", 7, CredentialVisibility.PRIVATE,
                Instant.now(), Instant.now(), Set.of("username", "password"), Set.of(), true);

        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), descriptor, true, 10 * 1024 * 1024L, () -> { });
        PasswordField secret = (PasswordField) dialog.getFieldsForm().getFieldComponent("password");

        assertTrue(secret.getValue().isEmpty());
        assertTrue(dialog.save());

        assertFalse(capturedRequest.get().values().containsKey("password"));
        assertEquals("alice", capturedRequest.get().values().get("username"));
        assertEquals("", capturedRequest.get().description());
        verify(service).findNonSensitiveValues(new org.pacos.base.credential.CredentialReference(descriptor.id()),
                CredentialAccessContext.forUser(7));
    }

    @Test
    void whenCreateCredentialFailsThenKeepDialogOpenAndDoNotRefresh() {
        when(service.getAvailableTypes()).thenReturn(List.of(new CredentialTypeRegistry().getRequired("bearer-token")));
        when(service.create(any(), any())).thenThrow(new IllegalStateException("storage unavailable"));
        AtomicInteger saved = new AtomicInteger();
        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), null, true, 10 * 1024 * 1024L, saved::incrementAndGet);
        dialog.getNameField().setValue("Production");
        ((PasswordField) dialog.getFieldsForm().getFieldComponent("token")).setValue("secret");

        assertFalse(dialog.save());

        assertEquals(0, saved.get());
        assertTrue(((PasswordField) dialog.getFieldsForm().getFieldComponent("token")).getValue().isEmpty());
    }

    @Test
    void whenCreateCredentialThenPersistFileAndClearFileBuffer() {
        when(service.getAvailableTypes()).thenReturn(List.of(new CredentialTypeRegistry().getRequired("client-certificate")));
        AtomicReference<byte[]> capturedContent = new AtomicReference<>();
        AtomicReference<CredentialFileInput> capturedFile = new AtomicReference<>();
        when(service.create(any(), any())).thenAnswer(invocation -> {
            CredentialCreateRequest request = invocation.getArgument(0);
            CredentialFileInput file = request.files().get("certificate");
            capturedFile.set(file);
            capturedContent.set(file.content());
            return null;
        });
        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), null, true, 10 * 1024 * 1024L, () -> { });
        dialog.getNameField().setValue("Certificate");
        dialog.getFieldsForm().storeUploadedFile(
                "certificate", "client.pem", "application/x-pem-file", new byte[]{4, 5, 6});

        assertTrue(dialog.save());

        assertArrayEquals(new byte[]{4, 5, 6}, capturedContent.get());
        assertArrayEquals(new byte[]{0, 0, 0}, capturedFile.get().content());
    }

    @Test
    void whenEditCredentialThenSubmitOnlyRequestedChangesAndPreserveWriteOnlySecret() {
        CredentialTypeDefinition definition = new CredentialTypeRegistry().getRequired("oauth2-client");
        when(service.getAvailableTypes()).thenReturn(List.of(definition));
        when(service.findNonSensitiveValues(any(), any())).thenReturn(Map.of(
                "clientId", "client-123",
                "tokenUrl", "https://example.test/token",
                "scopes", Map.of("read", "true")));
        AtomicReference<CredentialUpdateRequest> capturedRequest = new AtomicReference<>();
        when(service.update(any(), any(), any())).thenAnswer(invocation -> {
            capturedRequest.set(invocation.getArgument(1));
            return null;
        });
        CredentialAdminDescriptor descriptor = new CredentialAdminDescriptor(
                UUID.randomUUID(), "OAuth", null, "oauth2-client", 7, CredentialVisibility.PRIVATE,
                Instant.now(), Instant.now(), Set.of("clientId", "clientSecret", "tokenUrl", "scopes"), Set.of(), false);

        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), descriptor, true, 10 * 1024 * 1024L, () -> { });
        Checkbox removeScopes = dialog.getFieldsForm().getRemoveFieldControl("scopes");
        removeScopes.setValue(true);

        assertTrue(dialog.save());

        CredentialUpdateRequest request = capturedRequest.get();
        assertEquals(Set.of("scopes"), request.removeFields());
        assertEquals("client-123", request.values().get("clientId"));
        assertFalse(request.values().containsKey("clientSecret"));
        assertFalse(request.values().containsKey("scopes"));
        verify(service).update(new CredentialReference(descriptor.id()), request, CredentialAccessContext.forUser(7));
    }

    @Test
    void whenCreateModalConfirmationIsTriggeredThenPersistCredential() {
        when(service.getAvailableTypes()).thenReturn(List.of(new CredentialTypeRegistry().getRequired("bearer-token")));
        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), null, true, 10 * 1024 * 1024L, () -> { });
        dialog.getNameField().setValue("Production");
        ModalWindowConfig config = dialog.toWindowConfig();
        OnConfirmEvent confirmEvent = (OnConfirmEvent) windowStateObject(config, "confirmEvent");

        assertTrue(confirmEvent.confirm());

        verify(service).create(any(), eq(CredentialAccessContext.forUser(7)));
    }

    @Test
    void whenCreateCredentialSucceedsButRefreshFailsThenCloseDialog() {
        when(service.getAvailableTypes()).thenReturn(List.of(new CredentialTypeRegistry().getRequired("bearer-token")));
        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), null, true, 10 * 1024 * 1024L,
                () -> { throw new IllegalStateException("refresh failed"); });
        dialog.getNameField().setValue("Production");

        assertTrue(dialog.save());
    }

    @Test
    void whenCreateCredentialWithoutSelectedTypeThenKeepDialogOpen() {
        when(service.getAvailableTypes()).thenReturn(List.of(
                new CredentialTypeRegistry().getRequired("bearer-token"),
                new CredentialTypeRegistry().getRequired("basic-auth")));
        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), null, true, 10 * 1024 * 1024L, () -> { });
        dialog.getNameField().setValue("Production");

        assertFalse(dialog.save());

        verify(service, never()).create(any(), any());
    }

    @Test
    void whenEditCredentialWithoutManageAccessThenMakeVisibilityReadOnly() {
        CredentialTypeDefinition definition = new CredentialTypeRegistry().getRequired("basic-auth");
        when(service.getAvailableTypes()).thenReturn(List.of(definition));
        when(service.findNonSensitiveValues(any(), any())).thenReturn(Map.of());
        CredentialAdminDescriptor descriptor = new CredentialAdminDescriptor(
                UUID.randomUUID(), "Production", null, "basic-auth", 7, CredentialVisibility.PRIVATE,
                Instant.now(), Instant.now(), Set.of(), Set.of(), false);

        CredentialEditorDialog dialog = new CredentialEditorDialog(
                service, CredentialAccessContext.forUser(7), descriptor, false, 10 * 1024 * 1024L, () -> { });

        assertTrue(dialog.getVisibilityField().isReadOnly());
        assertTrue(dialog.getTypeField().isReadOnly());
    }

    private static int windowStateValue(ModalWindowConfig config, String name) {
        try {
            java.lang.reflect.Field field = config.getWindowState().getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.getInt(config.getWindowState());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private static Object windowStateObject(ModalWindowConfig config, String name) {
        try {
            java.lang.reflect.Field field = config.getWindowState().getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(config.getWindowState());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}

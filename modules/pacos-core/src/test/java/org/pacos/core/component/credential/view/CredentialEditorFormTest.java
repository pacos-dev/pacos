package org.pacos.core.component.credential.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.config.VaadinMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.core.component.credential.service.CredentialFieldDefinition;
import org.pacos.core.component.credential.service.CredentialFieldType;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;
import org.pacos.core.component.credential.service.CredentialTypeRegistry;
import org.pacos.core.component.credential.service.CredentialVisibility;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;

class CredentialEditorFormTest {

    @BeforeEach
    void setUp() {
        VaadinMock.mockSystem();
    }

    @Test
    void whenCreateFileFieldThenApplyConfiguredUploadLimit() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 4096);
        form.setDefinition(new CredentialTypeRegistry().getRequired("client-certificate"));

        Upload upload = form.getUpload("certificate");

        assertEquals(4096, upload.getMaxFileSize());
    }

    @Test
    void whenUploadLimitExceedsVaadinMaximumThenClampToIntegerMaximum() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), Long.MAX_VALUE);
        form.setDefinition(new CredentialTypeRegistry().getRequired("client-certificate"));

        assertEquals(Integer.MAX_VALUE, form.getUpload("certificate").getMaxFileSize());
    }

    @Test
    void whenConfiguredSecretFieldThenKeepItWriteOnly() {
        CredentialAdminDescriptor descriptor = new CredentialAdminDescriptor(
                UUID.randomUUID(), "Production", null, "basic-auth", 7, CredentialVisibility.PRIVATE,
                Instant.now(), Instant.now(), Set.of("username", "password"), Set.of(), false);
        CredentialEditorForm form = new CredentialEditorForm(descriptor, Map.of("username", "alice"), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("basic-auth"));

        assertEquals("alice", ((TextField) form.getFieldComponent("username")).getValue());
        assertTrue(((PasswordField) form.getFieldComponent("password")).getValue().isEmpty());
    }

    @Test
    void whenCollectKeyValueFieldThenParsePairs() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("oauth2-client"));
        TextArea scopes = (TextArea) form.getFieldComponent("scopes");
        scopes.setValue("read=true\nwrite=false");

        assertEquals(Map.of("read", "true", "write", "false"), form.collectValues().get("scopes"));
    }

    @Test
    void whenCollectKeyValueWithBlankKeyThenThrowException() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("oauth2-client"));
        ((TextArea) form.getFieldComponent("scopes")).setValue(" =value");

        assertThrows(IllegalArgumentException.class, form::collectValues);
    }

    @Test
    void whenCollectKeyValueWithDuplicateKeyThenThrowException() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("oauth2-client"));
        ((TextArea) form.getFieldComponent("scopes")).setValue("read=true\nread=false");

        assertThrows(IllegalArgumentException.class, form::collectValues);
    }

    @Test
    void whenRemoveOptionalConfiguredFieldThenIncludeItsKeyInRemovalSet() {
        CredentialAdminDescriptor descriptor = new CredentialAdminDescriptor(
                UUID.randomUUID(), "OAuth", null, "oauth2-client", 7, CredentialVisibility.PRIVATE,
                Instant.now(), Instant.now(), Set.of("scopes"), Set.of(), false);
        CredentialEditorForm form = new CredentialEditorForm(descriptor, Map.of("scopes", Map.of("read", "true")), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("oauth2-client"));
        Checkbox remove = form.getRemoveFieldControl("scopes");

        remove.setValue(true);

        assertEquals(Set.of("scopes"), form.getFieldsToRemove());
        assertTrue(((TextArea) form.getFieldComponent("scopes")).getValue().isEmpty());
    }

    @Test
    void whenRequiredBooleanIsFalseThenIncludeFalseValue() {
        CredentialTypeDefinition definition = new CredentialTypeDefinition(
                "feature", "Feature", null,
                java.util.List.of(new CredentialFieldDefinition(
                        "enabled", "Enabled", CredentialFieldType.BOOLEAN, true, false, false)));
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(definition);

        assertEquals(Boolean.FALSE, form.collectValues().get("enabled"));
    }

    @Test
    void whenRemoveConfiguredFileThenDisableUploadAndMarkForRemoval() {
        CredentialAdminDescriptor descriptor = new CredentialAdminDescriptor(
                UUID.randomUUID(), "Certificate", null, "test-certificate", 7, CredentialVisibility.PRIVATE,
                Instant.now(), Instant.now(), Set.of(), Set.of("certificate"), false);
        CredentialTypeDefinition definition = new CredentialTypeDefinition(
                "test-certificate", "Test Certificate", null,
                java.util.List.of(new CredentialFieldDefinition(
                        "certificate", "Certificate", CredentialFieldType.FILE, false, false, false)));
        CredentialEditorForm form = new CredentialEditorForm(descriptor, Map.of(), 1024);
        form.setDefinition(definition);
        Checkbox remove = form.getRemoveFileControl("certificate");
        Upload upload = form.getUpload("certificate");

        remove.setValue(true);

        assertEquals(Set.of("certificate"), form.getFilesToRemove());
        assertFalse(upload.isEnabled());
        remove.setValue(false);
        assertTrue(upload.isEnabled());
    }

    @Test
    void whenCollectFailsAfterReadingSecretThenWipePartiallyCollectedSecret() {
        CredentialTypeDefinition definition = new CredentialTypeDefinition(
                "secret-key-values", "Secret and key-values", null,
                java.util.List.of(
                        new CredentialFieldDefinition("secret", "Secret", CredentialFieldType.SECRET, false, true, false),
                        new CredentialFieldDefinition("values", "Values", CredentialFieldType.KEY_VALUE, false, false, true)));
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(definition);
        ((PasswordField) form.getFieldComponent("secret")).setValue("sensitive");
        ((TextArea) form.getFieldComponent("values")).setValue("=invalid");
        Map<String, Object> values = new java.util.LinkedHashMap<>();

        assertThrows(IllegalArgumentException.class, () -> form.collectValues(values));

        assertArrayEquals(new char["sensitive".length()], (char[]) values.get("secret"));
    }

    @Test
    void whenCollectNumberFieldThenIncludeNumberValue() {
        CredentialTypeDefinition definition = new CredentialTypeDefinition(
                "numeric", "Numeric", null,
                java.util.List.of(new CredentialFieldDefinition(
                        "timeout", "Timeout", CredentialFieldType.NUMBER, false, false, false)));
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(definition);
        ((NumberField) form.getFieldComponent("timeout")).setValue(12.5);

        assertEquals(12.5, form.collectValues().get("timeout"));
    }

    @Test
    void whenRemoveAndRestoreOptionalFieldsThenRestoreTheirNonSensitiveValues() {
        CredentialTypeDefinition definition = new CredentialTypeDefinition(
                "all-optional", "All optional", null,
                java.util.List.of(
                        new CredentialFieldDefinition("text", "Text", CredentialFieldType.TEXT, false, false, false),
                        new CredentialFieldDefinition("secret", "Secret", CredentialFieldType.SECRET, false, true, false),
                        new CredentialFieldDefinition("pairs", "Pairs", CredentialFieldType.KEY_VALUE, false, false, true),
                        new CredentialFieldDefinition("enabled", "Enabled", CredentialFieldType.BOOLEAN, false, false, false),
                        new CredentialFieldDefinition("number", "Number", CredentialFieldType.NUMBER, false, false, false),
                        new CredentialFieldDefinition("file", "File", CredentialFieldType.FILE, false, true, false)));
        CredentialAdminDescriptor descriptor = new CredentialAdminDescriptor(
                UUID.randomUUID(), "Existing", null, "all-optional", 7, CredentialVisibility.PRIVATE,
                Instant.now(), Instant.now(), Set.of("text", "secret", "pairs", "enabled", "number"), Set.of("file"), false);
        CredentialEditorForm form = new CredentialEditorForm(descriptor,
                Map.of("text", "original", "pairs", Map.of("key", "value"), "enabled", true, "number", 12.5),
                1024);
        form.setDefinition(definition);
        ((PasswordField) form.getFieldComponent("secret")).setValue("replacement");

        for (String key : java.util.List.of("text", "secret", "pairs", "enabled", "number")) {
            form.getRemoveFieldControl(key).setValue(true);
            form.getRemoveFieldControl(key).setValue(false);
        }
        form.getRemoveFileControl("file").setValue(true);
        form.getRemoveFileControl("file").setValue(false);

        assertEquals("original", ((TextField) form.getFieldComponent("text")).getValue());
        assertTrue(((PasswordField) form.getFieldComponent("secret")).getValue().isEmpty());
        assertEquals("key=value\n", ((TextArea) form.getFieldComponent("pairs")).getValue());
        assertTrue(((Checkbox) form.getFieldComponent("enabled")).getValue());
        assertEquals(12.5, ((NumberField) form.getFieldComponent("number")).getValue());
        assertTrue(form.getFieldsToRemove().isEmpty());
        assertTrue(form.getFilesToRemove().isEmpty());
        assertTrue(form.getUpload("file").isEnabled());
    }

    @Test
    void whenUploadHandlerProcessesFileThenStoreContentAndWipeCallbackBuffer() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("client-certificate"));
        byte[] content = new byte[]{1, 2, 3};

        form.handleUploadedFile("certificate", "certificate.pem", "application/x-pem-file", content);

        assertArrayEquals(new byte[]{0, 0, 0}, content);
        assertArrayEquals(new byte[]{1, 2, 3},
                form.getUploadedFiles().get("certificate").content());
    }

    @Test
    void whenUploadHandlerCannotStoreFileThenWipeCallbackBuffer() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("client-certificate"));
        byte[] content = new byte[]{1, 2, 3};

        form.handleUploadedFile("certificate", "", "application/x-pem-file", content);

        assertArrayEquals(new byte[]{0, 0, 0}, content);
        assertTrue(form.getUploadedFiles().isEmpty());
    }

    @Test
    void whenStoreUploadedFileThenExposeItsContentAndClearItWithForm() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("client-certificate"));
        byte[] content = new byte[]{1, 2, 3};
        form.storeUploadedFile("certificate", "certificate.pem", "application/x-pem-file", content);
        org.pacos.core.component.credential.service.CredentialFileInput file = form.getUploadedFiles().get("certificate");

        assertArrayEquals(content, file.content());

        form.clearUploadedFiles();

        assertArrayEquals(new byte[]{0, 0, 0}, file.content());
        assertTrue(form.getUploadedFiles().isEmpty());
    }

    @Test
    void whenReplaceUploadedFileThenClearPreviousFileBuffer() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("client-certificate"));
        form.storeUploadedFile("certificate", "first.pem", "application/x-pem-file", new byte[]{1});
        org.pacos.core.component.credential.service.CredentialFileInput previous = form.getUploadedFiles().get("certificate");

        form.storeUploadedFile("certificate", "second.pem", "application/x-pem-file", new byte[]{2});

        assertArrayEquals(new byte[]{0}, previous.content());
        assertArrayEquals(new byte[]{2}, form.getUploadedFiles().get("certificate").content());
    }

    @Test
    void whenCollectKeyValueWithoutSeparatorThenThrowException() {
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(new CredentialTypeRegistry().getRequired("oauth2-client"));
        ((TextArea) form.getFieldComponent("scopes")).setValue("invalid-line");

        assertThrows(IllegalArgumentException.class, form::collectValues);
    }

    @Test
    void whenSingleValueKeyValueFieldContainsMultipleEntriesThenThrowException() {
        CredentialTypeDefinition definition = new CredentialTypeDefinition(
                "single-pair", "Single pair", null,
                java.util.List.of(new CredentialFieldDefinition(
                        "pair", "Pair", CredentialFieldType.KEY_VALUE, false, false, false)));
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(definition);
        ((TextArea) form.getFieldComponent("pair")).setValue("first=1\nsecond=2");

        assertThrows(IllegalArgumentException.class, form::collectValues);
    }

    @Test
    void whenRequiredKeyValueAndNumberFieldsAreCreatedThenShowRequiredIndicators() {
        CredentialTypeDefinition definition = new CredentialTypeDefinition(
                "required-fields", "Required fields", null,
                java.util.List.of(
                        new CredentialFieldDefinition("pair", "Pair", CredentialFieldType.KEY_VALUE, true, false, true),
                        new CredentialFieldDefinition("count", "Count", CredentialFieldType.NUMBER, true, false, false)));
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(definition);

        assertTrue(((TextArea) form.getFieldComponent("pair")).isRequiredIndicatorVisible());
        assertTrue(((NumberField) form.getFieldComponent("count")).isRequiredIndicatorVisible());
    }

    @Test
    void whenChangingCredentialTypeThenClearOldSensitiveAndUploadedBuffers() {
        CredentialTypeDefinition first = new CredentialTypeDefinition(
                "first", "First", null,
                java.util.List.of(
                        new CredentialFieldDefinition("secret", "Secret", CredentialFieldType.SECRET, false, true, false),
                        new CredentialFieldDefinition("file", "File", CredentialFieldType.FILE, false, true, false)));
        CredentialTypeDefinition second = new CredentialTypeDefinition(
                "second", "Second", null,
                java.util.List.of(new CredentialFieldDefinition(
                        "name", "Name", CredentialFieldType.TEXT, false, false, false)));
        CredentialEditorForm form = new CredentialEditorForm(null, Map.of(), 1024);
        form.setDefinition(first);
        PasswordField secret = (PasswordField) form.getFieldComponent("secret");
        secret.setValue("private-value");
        form.storeUploadedFile("file", "private.key", "application/octet-stream", new byte[]{7, 8});
        org.pacos.core.component.credential.service.CredentialFileInput uploaded = form.getUploadedFiles().get("file");

        form.setDefinition(second);

        assertTrue(secret.getValue().isEmpty());
        assertArrayEquals(new byte[]{0, 0}, uploaded.content());
        assertTrue(form.getUploadedFiles().isEmpty());
        assertTrue(form.getFieldComponent("name") instanceof TextField);
    }

    @Test
    void whenMaximumFileSizeIsNotPositiveThenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> new CredentialEditorForm(null, Map.of(), 0));
    }
}

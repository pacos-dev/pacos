package org.pacos.core.component.credential.view;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.core.component.credential.service.CredentialFieldDefinition;
import org.pacos.core.component.credential.service.CredentialFieldType;
import org.pacos.core.component.credential.service.CredentialFileInput;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Builds credential-specific controls and converts their current state into an update payload.
 *
 * <p>The form tracks optional removals and owns temporary secret/file buffers. Changing the selected
 * type discards the previous form state and clears sensitive values and upload buffers before the
 * old controls are detached.</p>
 */
final class CredentialEditorForm extends VerticalLayout {

    private final CredentialAdminDescriptor descriptor;
    private final HashMap<String, Serializable> nonSensitiveValues;
    private final long maxFileSizeBytes;
    private final Map<String, Component> fieldComponents = new LinkedHashMap<>();
    private final Map<String, CredentialFileInput> uploadedFiles = new LinkedHashMap<>();
    private final Map<String, Checkbox> removeFieldControls = new LinkedHashMap<>();
    private final Map<String, Checkbox> removeFileControls = new LinkedHashMap<>();
    private final Map<String, Upload> uploads = new LinkedHashMap<>();
    private final Set<String> touchedBooleanFields = new LinkedHashSet<>();
    private CredentialTypeDefinition definition;

    CredentialEditorForm(
            CredentialAdminDescriptor descriptor, Map<String, Object> nonSensitiveValues, long maxFileSizeBytes) {
        if (maxFileSizeBytes < 1) {
            throw new IllegalArgumentException("Maximum credential file size must be positive");
        }
        this.descriptor = descriptor;
        this.nonSensitiveValues = new HashMap<>();
        nonSensitiveValues.forEach((key, value) -> this.nonSensitiveValues.put(key, serializableValue(value)));
        this.maxFileSizeBytes = maxFileSizeBytes;
        setPadding(false);
        setSpacing(true);
        setWidthFull();
        addDetachListener(event -> {
            clearSensitiveInputs();
            clearUploadedFiles();
        });
    }

    void setDefinition(CredentialTypeDefinition definition) {
        clearSensitiveInputs();
        clearUploadedFiles();
        this.definition = definition;
        removeAll();
        fieldComponents.clear();
        removeFieldControls.clear();
        removeFileControls.clear();
        uploads.clear();
        touchedBooleanFields.clear();
        if (definition == null) {
            return;
        }

        definition.fields().forEach(this::addField);
    }

    private static Serializable serializableValue(Object value) {
        if (value == null || value instanceof Serializable) {
            return (Serializable) value;
        }
        throw new IllegalArgumentException("Credential form values must be serializable");
    }

    private void addField(CredentialFieldDefinition field) {
        Component control = createFieldControl(field);
        fieldComponents.put(field.key(), control);
        add(control);
        addRemovalControl(field);
    }

    private void addRemovalControl(CredentialFieldDefinition field) {
        if (field.type() == CredentialFieldType.FILE) {
            addFileRemovalControl(field);
        } else {
            addValueRemovalControl(field);
        }
    }

    private void addFileRemovalControl(CredentialFieldDefinition field) {
        if (descriptor == null || !descriptor.configuredFiles().contains(field.key()) || field.required()) {
            return;
        }
        Checkbox remove = new Checkbox("Remove existing file");
        remove.addValueChangeListener(event -> {
            if (Boolean.TRUE.equals(event.getValue())) {
                clearUploadedFile(field.key());
            }
            Upload upload = uploads.get(field.key());
            if (upload != null) {
                upload.setEnabled(!Boolean.TRUE.equals(event.getValue()));
            }
        });
        removeFileControls.put(field.key(), remove);
        add(remove);
    }

    private void addValueRemovalControl(CredentialFieldDefinition field) {
        if (descriptor == null || !descriptor.configuredFields().contains(field.key()) || field.required()) {
            return;
        }
        Checkbox remove = new Checkbox("Remove existing value");
        remove.addValueChangeListener(event -> {
            if (Boolean.TRUE.equals(event.getValue())) {
                clearField(field);
            } else {
                restoreField(field);
            }
        });
        removeFieldControls.put(field.key(), remove);
        add(remove);
    }

    Map<String, Object> collectValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        collectValues(values);
        return values;
    }

    void collectValues(Map<String, Object> values) {
        if (definition == null) {
            throw new IllegalArgumentException("Credential type is required");
        }
        try {
            for (CredentialFieldDefinition field : definition.fields()) {
                collectFieldValue(field, fieldComponents.get(field.key()), values);
            }
        } catch (RuntimeException exception) {
            clearSecretValues(values);
            throw exception;
        }
    }

    private void collectFieldValue(CredentialFieldDefinition field, Component component, Map<String, Object> values) {
        switch (field.type()) {
            case TEXT -> collectTextValue(field, (TextField) component, values);
            case SECRET -> collectSecretValue(field, (PasswordField) component, values);
            case KEY_VALUE -> collectKeyValue(field, (TextArea) component, values);
            case BOOLEAN -> collectBooleanValue(field, (Checkbox) component, values);
            case NUMBER -> collectNumberValue(field, (NumberField) component, values);
            case FILE -> {
                // File inputs are submitted separately through getUploadedFiles().
            }
        }
    }

    private static void collectTextValue(CredentialFieldDefinition field, TextField component, Map<String, Object> values) {
        String value = component.getValue();
        if (value != null && !value.isBlank()) {
            values.put(field.key(), value);
        }
    }

    private static void collectSecretValue(CredentialFieldDefinition field, PasswordField component, Map<String, Object> values) {
        String value = component.getValue();
        if (value != null && !value.isEmpty()) {
            values.put(field.key(), value.toCharArray());
        }
    }

    private static void collectKeyValue(CredentialFieldDefinition field, TextArea component, Map<String, Object> values) {
        String value = component.getValue();
        if (value != null && !value.isBlank()) {
            values.put(field.key(), parseKeyValue(value, field.multiple()));
        }
    }

    private void collectBooleanValue(CredentialFieldDefinition field, Checkbox component, Map<String, Object> values) {
        if (descriptor == null || touchedBooleanFields.contains(field.key())) {
            values.put(field.key(), component.getValue());
        }
    }

    private static void collectNumberValue(CredentialFieldDefinition field, NumberField component, Map<String, Object> values) {
        Double value = component.getValue();
        if (value != null) {
            values.put(field.key(), value);
        }
    }

    Map<String, CredentialFileInput> getUploadedFiles() {
        return new LinkedHashMap<>(uploadedFiles);
    }

    void storeUploadedFile(String fieldKey, String originalFilename, String contentType, byte[] content) {
        CredentialFileInput previous = uploadedFiles.put(
                fieldKey, new CredentialFileInput(originalFilename, contentType, content));
        if (previous != null) {
            previous.clear();
        }
    }

    Set<String> getFieldsToRemove() {
        return checkedKeys(removeFieldControls);
    }

    Set<String> getFilesToRemove() {
        return checkedKeys(removeFileControls);
    }

    void clearSensitiveInputs() {
        fieldComponents.values().stream()
                .filter(PasswordField.class::isInstance)
                .map(PasswordField.class::cast)
                .forEach(PasswordField::clear);
    }

    void clearUploadedFiles() {
        uploadedFiles.values().forEach(CredentialFileInput::clear);
        uploadedFiles.clear();
    }

    private void clearUploadedFile(String fieldKey) {
        CredentialFileInput file = uploadedFiles.remove(fieldKey);
        if (file != null) {
            file.clear();
        }
    }

    Component getFieldComponent(String fieldKey) {
        return fieldComponents.get(fieldKey);
    }

    Checkbox getRemoveFieldControl(String fieldKey) {
        return removeFieldControls.get(fieldKey);
    }

    Checkbox getRemoveFileControl(String fieldKey) {
        return removeFileControls.get(fieldKey);
    }

    Upload getUpload(String fieldKey) {
        return uploads.get(fieldKey);
    }

    private Component createFieldControl(CredentialFieldDefinition field) {
        boolean configured = descriptor != null && (field.type() == CredentialFieldType.FILE
                ? descriptor.configuredFiles().contains(field.key())
                : descriptor.configuredFields().contains(field.key()));
        String helper = configured
                ? "Already configured. Leave empty to keep the current value."
                : null;

        return switch (field.type()) {
            case TEXT -> createTextField(field, configured, helper);
            case SECRET -> createSecretField(field, configured, helper);
            case KEY_VALUE -> createKeyValueField(field, configured, helper);
            case BOOLEAN -> createBooleanField(field, configured);
            case NUMBER -> createNumberField(field, configured, helper);
            case FILE -> createFileUpload(field, configured);
        };
    }

    private TextField createTextField(CredentialFieldDefinition field, boolean configured, String helper) {
        TextField input = new TextField(field.label());
        input.setWidthFull();
        input.setRequiredIndicatorVisible(field.required() && !configured);
        input.setHelperText(helper);
        if (nonSensitiveValues.get(field.key()) instanceof String text) {
            input.setValue(text);
        }
        return input;
    }

    private static PasswordField createSecretField(CredentialFieldDefinition field, boolean configured, String helper) {
        PasswordField input = new PasswordField(field.label());
        input.setWidthFull();
        input.setRequiredIndicatorVisible(field.required() && !configured);
        input.setHelperText(helper);
        return input;
    }

    private TextArea createKeyValueField(CredentialFieldDefinition field, boolean configured, String helper) {
        TextArea input = new TextArea(field.label());
        input.setWidthFull();
        input.setRequiredIndicatorVisible(field.required() && !configured);
        input.setHelperText("Enter " + (field.multiple() ? "one or more" : "one")
                + " key=value pair" + (field.multiple() ? "s" : "") + ". "
                + (helper == null ? "" : helper));
        input.setValue(formatKeyValue(nonSensitiveValues.get(field.key())));
        return input;
    }

    private Checkbox createBooleanField(CredentialFieldDefinition field, boolean configured) {
        Checkbox input = new Checkbox(field.label() + (field.required() && !configured ? " (required)" : ""));
        if (nonSensitiveValues.get(field.key()) instanceof Boolean booleanValue) {
            input.setValue(booleanValue);
        }
        input.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                touchedBooleanFields.add(field.key());
            }
        });
        return input;
    }

    private NumberField createNumberField(CredentialFieldDefinition field, boolean configured, String helper) {
        NumberField input = new NumberField(field.label());
        input.setWidthFull();
        input.setRequiredIndicatorVisible(field.required() && !configured);
        input.setHelperText(helper);
        if (nonSensitiveValues.get(field.key()) instanceof Number number) {
            input.setValue(number.doubleValue());
        }
        return input;
    }

    private void clearField(CredentialFieldDefinition field) {
        Component component = fieldComponents.get(field.key());
        switch (field.type()) {
            case TEXT -> ((TextField) component).clear();
            case SECRET -> ((PasswordField) component).clear();
            case KEY_VALUE -> ((TextArea) component).clear();
            case BOOLEAN -> {
                ((Checkbox) component).setValue(false);
                touchedBooleanFields.remove(field.key());
            }
            case NUMBER -> ((NumberField) component).clear();
            case FILE -> clearUploadedFile(field.key());
        }
    }

    private void restoreField(CredentialFieldDefinition field) {
        Component component = fieldComponents.get(field.key());
        Object value = nonSensitiveValues.get(field.key());
        switch (value) {
            case null -> {
                // There is no stored value to restore.
            }
            case String text when field.type() == CredentialFieldType.TEXT ->
                    ((TextField) component).setValue(text);
            case Boolean booleanValue when field.type() == CredentialFieldType.BOOLEAN ->
                    ((Checkbox) component).setValue(booleanValue);
            case Number number when field.type() == CredentialFieldType.NUMBER ->
                    ((NumberField) component).setValue(number.doubleValue());
            default -> {
                if (field.type() == CredentialFieldType.KEY_VALUE) {
                    ((TextArea) component).setValue(formatKeyValue(value));
                }
            }
        }
    }

    private Component createFileUpload(CredentialFieldDefinition field, boolean configured) {
        Upload upload = new Upload(UploadHandler.inMemory((metadata, content) ->
                handleUploadedFile(field.key(), metadata.fileName(), metadata.contentType(), content))
                .whenComplete(success -> {
                    if (!Boolean.TRUE.equals(success)) {
                        NotificationUtils.error("Cannot upload file. Please try again.");
                    }
                }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) Math.min(maxFileSizeBytes, Integer.MAX_VALUE));
        upload.setDropAllowed(true);
        upload.setWidthFull();
        uploads.put(field.key(), upload);
        String label = field.label() + (field.required() ? " (required)" : "")
                + (configured ? " — file already configured; upload to replace" : "");
        VerticalLayout wrapper = new VerticalLayout(new Span(label), upload);
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        return wrapper;
    }

    void handleUploadedFile(String fieldKey, String originalFilename, String contentType, byte[] content) {
        try {
            storeUploadedFile(fieldKey, originalFilename, contentType, content);
            Checkbox remove = removeFileControls.get(fieldKey);
            if (remove != null && Boolean.TRUE.equals(remove.getValue())) {
                remove.setValue(false);
            }
        } catch (RuntimeException exception) {
            NotificationUtils.error("Cannot process uploaded file: " + exception.getMessage());
        } finally {
            Arrays.fill(content, (byte) 0);
        }
    }

    private static String formatKeyValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        map.forEach((key, entryValue) -> result.append(key).append('=').append(entryValue).append('\n'));
        return result.toString();
    }

    static Map<String, String> parseKeyValue(String input, boolean multiple) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : input.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator < 1) {
                throw new IllegalArgumentException("Each key-value entry must use key=value format");
            }
            String key = line.substring(0, separator).trim();
            if (key.isEmpty()) {
                throw new IllegalArgumentException("Key-value entry key cannot be blank");
            }
            if (!multiple && !values.isEmpty()) {
                throw new IllegalArgumentException("Only one key-value entry is allowed");
            }
            if (values.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate key-value entry: " + key);
            }
            values.put(key, line.substring(separator + 1).trim());
        }
        return values;
    }

    private static Set<String> checkedKeys(Map<String, Checkbox> controls) {
        Set<String> keys = new LinkedHashSet<>();
        controls.forEach((key, checkbox) -> {
            if (Boolean.TRUE.equals(checkbox.getValue())) {
                keys.add(key);
            }
        });
        return keys;
    }

    private static void clearSecretValues(Map<String, Object> values) {
        values.values().stream()
                .filter(char[].class::isInstance)
                .map(char[].class::cast)
                .forEach(secret -> Arrays.fill(secret, '\0'));
    }
}

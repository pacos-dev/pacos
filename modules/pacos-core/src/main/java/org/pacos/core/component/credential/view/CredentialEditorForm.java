package org.pacos.core.component.credential.view;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.core.component.credential.service.CredentialFieldDefinition;
import org.pacos.core.component.credential.service.CredentialFieldType;
import org.pacos.core.component.credential.service.CredentialFileInput;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;
import org.pacos.base.utils.notification.NotificationUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Span;
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
    private final Map<String, Object> nonSensitiveValues;
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
        this.nonSensitiveValues = Map.copyOf(nonSensitiveValues);
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

        for (CredentialFieldDefinition field : definition.fields()) {
            Component control = createFieldControl(field);
            fieldComponents.put(field.key(), control);
            add(control);
            if (field.type() == CredentialFieldType.FILE) {
                if (descriptor != null && descriptor.configuredFiles().contains(field.key()) && !field.required()) {
                    Checkbox remove = new Checkbox("Remove existing file");
                    remove.addValueChangeListener(event -> {
                        if (event.getValue()) {
                            clearUploadedFile(field.key());
                        }
                        Upload upload = uploads.get(field.key());
                        if (upload != null) {
                            upload.setEnabled(!event.getValue());
                        }
                    });
                    removeFileControls.put(field.key(), remove);
                    add(remove);
                }
            } else if (descriptor != null && descriptor.configuredFields().contains(field.key()) && !field.required()) {
                Checkbox remove = new Checkbox("Remove existing value");
                remove.addValueChangeListener(event -> {
                    if (event.getValue()) {
                        clearField(field);
                    } else {
                        restoreField(field);
                    }
                });
                removeFieldControls.put(field.key(), remove);
                add(remove);
            }
        }
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
                Component component = fieldComponents.get(field.key());
                switch (field.type()) {
                    case TEXT -> {
                        String value = ((TextField) component).getValue();
                        if (value != null && !value.isBlank()) {
                            values.put(field.key(), value);
                        }
                    }
                    case SECRET -> {
                        String value = ((PasswordField) component).getValue();
                        if (value != null && !value.isEmpty()) {
                            values.put(field.key(), value.toCharArray());
                        }
                    }
                    case KEY_VALUE -> {
                        String value = ((TextArea) component).getValue();
                        if (value != null && !value.isBlank()) {
                            values.put(field.key(), parseKeyValue(value, field.multiple()));
                        }
                    }
                    case BOOLEAN -> {
                        if (descriptor == null || touchedBooleanFields.contains(field.key())) {
                            values.put(field.key(), ((Checkbox) component).getValue());
                        }
                    }
                    case NUMBER -> {
                        Double value = ((NumberField) component).getValue();
                        if (value != null) {
                            values.put(field.key(), value);
                        }
                    }
                    case FILE -> {
                    }
                }
            }
        } catch (RuntimeException exception) {
            clearSecretValues(values);
            throw exception;
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
            case TEXT -> {
                TextField input = new TextField(field.label());
                input.setWidthFull();
                input.setRequiredIndicatorVisible(field.required() && !configured);
                input.setHelperText(helper);
                Object value = nonSensitiveValues.get(field.key());
                if (value instanceof String text) {
                    input.setValue(text);
                }
                yield input;
            }
            case SECRET -> {
                PasswordField input = new PasswordField(field.label());
                input.setWidthFull();
                input.setRequiredIndicatorVisible(field.required() && !configured);
                input.setHelperText(helper);
                yield input;
            }
            case KEY_VALUE -> {
                TextArea input = new TextArea(field.label());
                input.setWidthFull();
                input.setRequiredIndicatorVisible(field.required() && !configured);
                input.setHelperText("Enter " + (field.multiple() ? "one or more" : "one")
                        + " key=value pair" + (field.multiple() ? "s" : "") + ". "
                        + (helper == null ? "" : helper));
                input.setValue(formatKeyValue(nonSensitiveValues.get(field.key())));
                yield input;
            }
            case BOOLEAN -> {
                Checkbox input = new Checkbox(field.label() + (field.required() && !configured ? " (required)" : ""));
                Object value = nonSensitiveValues.get(field.key());
                if (value instanceof Boolean booleanValue) {
                    input.setValue(booleanValue);
                }
                input.addValueChangeListener(event -> {
                    if (event.isFromClient()) {
                        touchedBooleanFields.add(field.key());
                    }
                });
                yield input;
            }
            case NUMBER -> {
                NumberField input = new NumberField(field.label());
                input.setWidthFull();
                input.setRequiredIndicatorVisible(field.required() && !configured);
                input.setHelperText(helper);
                Object value = nonSensitiveValues.get(field.key());
                if (value instanceof Number number) {
                    input.setValue(number.doubleValue());
                }
                yield input;
            }
            case FILE -> createFileUpload(field, configured);
        };
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
        Object value = nonSensitiveValues.get(field.key());
        Component component = fieldComponents.get(field.key());
        switch (field.type()) {
            case TEXT -> {
                if (value instanceof String text) {
                    ((TextField) component).setValue(text);
                }
            }
            case KEY_VALUE -> ((TextArea) component).setValue(formatKeyValue(value));
            case BOOLEAN -> {
                if (value instanceof Boolean booleanValue) {
                    ((Checkbox) component).setValue(booleanValue);
                }
            }
            case NUMBER -> {
                if (value instanceof Number number) {
                    ((NumberField) component).setValue(number.doubleValue());
                }
            }
            case SECRET, FILE -> {
            }
        }
    }

    private Component createFileUpload(CredentialFieldDefinition field, boolean configured) {
        Upload upload = new Upload(UploadHandler.inMemory((metadata, content) ->
                handleUploadedFile(field.key(), metadata.fileName(), metadata.contentType(), content))
                .whenComplete(success -> {
                    if (!success) {
                        NotificationUtils.error("Cannot upload file. Please try again.");
                    }
                }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) Math.min(maxFileSizeBytes, Integer.MAX_VALUE));
        upload.setDropAllowed(true);
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
            if (remove != null && remove.getValue()) {
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
            if (checkbox.getValue()) {
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

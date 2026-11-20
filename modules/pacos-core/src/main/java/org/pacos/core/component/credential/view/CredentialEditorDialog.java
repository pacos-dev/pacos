package org.pacos.core.component.credential.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialCreateRequest;
import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.base.credential.CredentialReference;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;
import org.pacos.core.component.credential.service.CredentialUpdateRequest;
import org.pacos.core.component.credential.service.CredentialVisibility;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.config.impl.ModalWindowConfig;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Edits credential metadata and delegates type-specific controls to {@link CredentialEditorForm}.
 *
 * <p>Secret inputs are write-only and cleared after every save attempt. A successful persistence operation
 * closes the modal even if the caller's list-refresh callback fails.</p>
 */
public class CredentialEditorDialog extends VerticalLayout {

    private static final int WINDOW_WIDTH = 760;
    private static final int WINDOW_HEIGHT = 720;

    private final CredentialVaultService credentialService;
    private final CredentialAccessContext accessContext;
    private final CredentialAdminDescriptor descriptor;
    private final Runnable onSaved;
    private final List<CredentialTypeDefinition> definitions;
    private final TextField nameField = new TextField("Name");
    private final TextArea descriptionField = new TextArea("Description");
    private final ComboBox<CredentialVisibility> visibilityField = new ComboBox<>("Visibility");
    private final ComboBox<CredentialTypeDefinition> typeField = new ComboBox<>("Type");
    private final CredentialEditorForm fieldsForm;

    public CredentialEditorDialog(
            CredentialVaultService credentialService,
            CredentialAccessContext accessContext,
            CredentialAdminDescriptor descriptor,
            boolean canManageAccess,
            long maxFileSizeBytes,
            Runnable onSaved) {
        this.credentialService = Objects.requireNonNull(credentialService);
        this.accessContext = Objects.requireNonNull(accessContext);
        this.descriptor = descriptor;
        this.onSaved = Objects.requireNonNull(onSaved);
        this.definitions = List.copyOf(credentialService.getAvailableTypes());
        Map<String, Object> nonSensitiveValues = descriptor == null ? Map.of()
                : credentialService.findNonSensitiveValues(new CredentialReference(descriptor.id()), accessContext);
        this.fieldsForm = new CredentialEditorForm(descriptor, nonSensitiveValues, maxFileSizeBytes);

        setSizeFull();
        getStyle().set("overflow-y", "auto");

        nameField.setWidthFull();
        nameField.setMaxLength(255);
        nameField.setRequiredIndicatorVisible(true);
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(2000);

        visibilityField.setItems(canManageAccess
                ? List.of(CredentialVisibility.values())
                : descriptor == null ? List.of(CredentialVisibility.PRIVATE) : List.of(descriptor.visibility()));
        visibilityField.setReadOnly(!canManageAccess);
        visibilityField.setItemLabelGenerator(value -> value == CredentialVisibility.PRIVATE ? "Private" : "Shared");
        visibilityField.setWidthFull();
        visibilityField.setRequiredIndicatorVisible(true);

        typeField.setItems(definitions);
        typeField.setItemLabelGenerator(CredentialTypeDefinition::label);
        typeField.setWidthFull();
        typeField.setRequiredIndicatorVisible(true);
        typeField.setReadOnly(descriptor != null);
        typeField.addValueChangeListener(event -> fieldsForm.setDefinition(event.getValue()));

        if (descriptor != null) {
            nameField.setValue(descriptor.name());
            descriptionField.setValue(descriptor.description() == null ? "" : descriptor.description());
            visibilityField.setValue(descriptor.visibility());
            typeField.setValue(definitions.stream()
                    .filter(definition -> definition.key().equals(descriptor.type()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unknown credential type: " + descriptor.type())));
        } else {
            visibilityField.setValue(CredentialVisibility.PRIVATE);
        }

        FormLayout metadata = new FormLayout(nameField, descriptionField, visibilityField, typeField);
        add(metadata, new Span("Credential fields"), fieldsForm);

        if (descriptor == null && definitions.size() == 1) {
            typeField.setValue(definitions.getFirst());
        }
    }

    public ModalWindowConfig toWindowConfig() {
        ModalWindowConfig config = new ModalWindowConfig();
        config.setTitle(descriptor == null ? "Create credential" : "Edit credential");
        config.setContent(this);
        config.getWindowState()
                .withWidth(WINDOW_WIDTH)
                .withHeight(WINDOW_HEIGHT)
                .setCancellable(true)
                .withConfirmEvent(this::save);
        config.getWindowState().withConfirmationBtnLabel(descriptor == null ? "Create" : "Save");
        return config;
    }

    /**
     * Persists the current form and reports whether the modal may close.
     *
     * @return {@code true} when persistence succeeded; {@code false} when validation or persistence failed
     */
    boolean save() {
        Map<String, Object> values = new LinkedHashMap<>();
        try {
            if (nameField.getValue() == null || nameField.getValue().isBlank()) {
                throw new IllegalArgumentException("Credential name is required");
            }
            if (typeField.getValue() == null) {
                throw new IllegalArgumentException("Credential type is required");
            }

            fieldsForm.collectValues(values);
            Map<String, org.pacos.core.component.credential.service.CredentialFileInput> files = fieldsForm.getUploadedFiles();
            if (descriptor == null) {
                credentialService.create(new CredentialCreateRequest(
                        nameField.getValue(),
                        emptyToNull(descriptionField.getValue()),
                        typeField.getValue().key(),
                        visibilityField.getValue(),
                        values,
                        files), accessContext);
            } else {
                Set<String> removeFields = fieldsForm.getFieldsToRemove();
                Set<String> removeFiles = fieldsForm.getFilesToRemove();
                removeFields.removeAll(values.keySet());
                removeFiles.removeAll(files.keySet());
                credentialService.update(new CredentialReference(descriptor.id()), new CredentialUpdateRequest(
                        nameField.getValue(),
                        descriptionField.getValue(),
                        visibilityField.getValue(),
                        values,
                        files,
                        removeFields,
                        removeFiles), accessContext);
            }
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception.getMessage());
            return false;
        } finally {
            values.values().stream()
                    .filter(char[].class::isInstance)
                    .map(char[].class::cast)
                    .forEach(secret -> java.util.Arrays.fill(secret, '\0'));
            fieldsForm.clearSensitiveInputs();
        }

        fieldsForm.clearUploadedFiles();
        try {
            onSaved.run();
        } catch (RuntimeException exception) {
            NotificationUtils.error("Credential was saved, but the list could not be refreshed: "
                    + exception.getMessage());
        }
        return true;
    }

    TextField getNameField() {
        return nameField;
    }

    TextArea getDescriptionField() {
        return descriptionField;
    }

    ComboBox<CredentialVisibility> getVisibilityField() {
        return visibilityField;
    }

    ComboBox<CredentialTypeDefinition> getTypeField() {
        return typeField;
    }

    CredentialEditorForm getFieldsForm() {
        return fieldsForm;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

package org.pacos.core.component.credential.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.pacos.base.credential.CredentialFieldDefinition;
import org.pacos.base.credential.CredentialFieldType;
import org.pacos.base.credential.CredentialFileInput;
import org.pacos.base.credential.CredentialTypeDefinition;
import org.pacos.base.credential.CredentialUpdateRequest;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.repository.CredentialFileRepository;
import org.pacos.core.component.credential.repository.CredentialValueRepository;

/** Validates credential payloads against their registered type definitions. */
final class CredentialPayloadValidator {

    private final CredentialValueRepository valueRepository;
    private final CredentialFileRepository fileRepository;

    CredentialPayloadValidator(CredentialValueRepository valueRepository, CredentialFileRepository fileRepository) {
        this.valueRepository = valueRepository;
        this.fileRepository = fileRepository;
    }

    void validateCreate(
            CredentialTypeDefinition definition,
            Map<String, Object> values,
            Map<String, CredentialFileInput> files) {
        validateKnownFields(definition, values.keySet(), files.keySet());
        for (CredentialFieldDefinition field : definition.fields()) {
            if (field.type() == CredentialFieldType.FILE) {
                if (field.required() && !files.containsKey(field.key())) {
                    throw new IllegalArgumentException("Required credential file is missing: " + field.key());
                }
                if (files.containsKey(field.key())) {
                    validateFile(files.get(field.key()));
                }
                continue;
            }
            Object value = values.get(field.key());
            if (field.required() && value == null) {
                throw new IllegalArgumentException("Required credential field is missing: " + field.key());
            }
            if (value != null) {
                validateValue(field, value);
            }
        }
    }

    void validateUpdate(
            CredentialTypeDefinition definition,
            CredentialUpdateRequest request,
            CredentialEntity credential) {
        validateKnownFields(definition, request.values().keySet(), request.files().keySet());
        validateKnownFields(definition, request.removeFields(), request.removeFiles());
        validateNoConflicts(request);

        request.values().forEach((key, value) -> validateValue(definitionField(definition, key), value));
        request.files().forEach((key, file) -> {
            CredentialFieldDefinition field = definitionField(definition, key);
            if (field.type() != CredentialFieldType.FILE) {
                throw new IllegalArgumentException("Credential field is not a file: " + field.key());
            }
            validateFile(file);
        });
        request.removeFields().forEach(key -> {
            CredentialFieldDefinition field = definitionField(definition, key);
            if (field.type() == CredentialFieldType.FILE || field.required()) {
                throw new IllegalArgumentException("Required or file field cannot be removed: " + key);
            }
        });
        request.removeFiles().forEach(key -> {
            CredentialFieldDefinition field = definitionField(definition, key);
            if (field.type() != CredentialFieldType.FILE || field.required()) {
                throw new IllegalArgumentException("Required or non-file field cannot be removed: " + key);
            }
        });

        validateRequiredFieldsAfterUpdate(definition, request, credential);
    }

    private static void validateNoConflicts(CredentialUpdateRequest request) {
        Set<String> conflicts = new HashSet<>(request.values().keySet());
        conflicts.retainAll(request.removeFields());
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Credential fields cannot be updated and removed together: " + conflicts);
        }
        conflicts = new HashSet<>(request.files().keySet());
        conflicts.retainAll(request.removeFiles());
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Credential files cannot be replaced and removed together: " + conflicts);
        }
    }

    private void validateRequiredFieldsAfterUpdate(
            CredentialTypeDefinition definition, CredentialUpdateRequest request, CredentialEntity credential) {
        Set<String> existingValues = valueRepository.findAllByCredential_Id(credential.getId()).stream()
                .map(value -> value.getFieldKey())
                .collect(Collectors.toSet());
        Set<String> existingFiles = fileRepository.findAllByCredential_Id(credential.getId()).stream()
                .map(file -> file.getFieldKey())
                .collect(Collectors.toSet());

        existingValues.removeAll(request.removeFields());
        existingValues.addAll(request.values().keySet());
        existingFiles.removeAll(request.removeFiles());
        existingFiles.addAll(request.files().keySet());

        for (CredentialFieldDefinition field : definition.fields()) {
            if (!field.required()) {
                continue;
            }
            boolean present = field.type() == CredentialFieldType.FILE
                    ? existingFiles.contains(field.key())
                    : existingValues.contains(field.key());
            if (!present) {
                String kind = field.type() == CredentialFieldType.FILE ? "file" : "field";
                throw new IllegalArgumentException("Required credential " + kind + " is missing: " + field.key());
            }
        }
    }

    private static void validateKnownFields(
            CredentialTypeDefinition definition, Set<String> valueFields, Set<String> fileFields) {
        Set<String> known = definition.fields().stream()
                .map(CredentialFieldDefinition::key)
                .collect(Collectors.toSet());
        Set<String> supplied = new HashSet<>(valueFields);
        supplied.addAll(fileFields);
        if (!known.containsAll(supplied)) {
            supplied.removeAll(known);
            throw new IllegalArgumentException("Unknown credential fields: " + supplied);
        }
        for (String key : valueFields) {
            if (definitionField(definition, key).type() == CredentialFieldType.FILE) {
                throw new IllegalArgumentException("Credential field must be supplied as a file: " + key);
            }
        }
        for (String key : fileFields) {
            if (definitionField(definition, key).type() != CredentialFieldType.FILE) {
                throw new IllegalArgumentException("Credential field does not accept a file: " + key);
            }
        }
    }

    private static void validateValue(CredentialFieldDefinition field, Object value) {
        boolean valid = switch (field.type()) {
            case TEXT -> value instanceof String;
            case SECRET -> value instanceof char[];
            case KEY_VALUE -> value instanceof Map<?, ?> map
                    && map.entrySet().stream().allMatch(entry -> entry.getKey() instanceof String
                    && entry.getValue() instanceof String);
            case BOOLEAN -> value instanceof Boolean;
            case NUMBER -> value instanceof Number;
            case FILE -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid value type for credential field: " + field.key());
        }
        if (field.required() && (value instanceof String text && text.isBlank()
                || value instanceof char[] secret && secret.length == 0
                || value instanceof Map<?, ?> map && map.isEmpty())) {
            throw new IllegalArgumentException("Required credential field cannot be empty: " + field.key());
        }
    }

    private static void validateFile(CredentialFileInput file) {
        if (file == null) {
            throw new IllegalArgumentException("Credential file is required");
        }
        if (file.size() == 0) {
            throw new IllegalArgumentException("Credential file cannot be empty");
        }
        if (file.originalFilename().length() > 255
                || file.contentType() != null && file.contentType().length() > 255) {
            throw new IllegalArgumentException("Credential file metadata is too long");
        }
    }

    private static CredentialFieldDefinition definitionField(CredentialTypeDefinition definition, String key) {
        return definition.fields().stream()
                .filter(field -> field.key().equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown credential field: " + key));
    }
}

package org.pacos.core.component.credential.service;

/**
 * Value representations supported by the credential editor and encrypted storage.
 *
 * <p>Secret and file values are treated as sensitive material and are never prefilled in the editor.</p>
 */
public enum CredentialFieldType {
    TEXT,
    SECRET,
    KEY_VALUE,
    FILE,
    BOOLEAN,
    NUMBER
}

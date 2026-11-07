package org.pacos.core.component.credential.security;

public record StoredCredentialFile(String storageReference, long size, String checksum) {
}

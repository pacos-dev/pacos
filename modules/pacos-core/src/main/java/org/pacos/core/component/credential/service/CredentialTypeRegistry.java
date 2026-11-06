package org.pacos.core.component.credential.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pacos.core.component.credential.service.CredentialFieldDefinition;
import org.pacos.core.component.credential.service.CredentialFieldType;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;
import org.springframework.stereotype.Component;

/**
 * Provides the built-in credential type definitions available to the Vault.
 */
@Component
public class CredentialTypeRegistry {

    private final Map<String, CredentialTypeDefinition> definitions;

    /** Initializes the built-in credential types. */
    public CredentialTypeRegistry() {
        Map<String, CredentialTypeDefinition> types = new LinkedHashMap<>();
        register(types, new CredentialTypeDefinition("basic-auth", "Basic Auth", "HTTP Basic authentication",
                List.of(
                        field("username", "Username", CredentialFieldType.TEXT, true, false),
                        field("password", "Password", CredentialFieldType.SECRET, true, true))));
        register(types, new CredentialTypeDefinition("bearer-token", "Bearer Token", "Bearer access token",
                List.of(field("token", "Token", CredentialFieldType.SECRET, true, true))));
        register(types, new CredentialTypeDefinition("api-key", "API Key", "API key used in a header or query parameter",
                List.of(
                        field("location", "Location", CredentialFieldType.TEXT, true, false),
                        field("name", "Parameter name", CredentialFieldType.TEXT, true, false),
                        field("value", "API key", CredentialFieldType.SECRET, true, true))));
        register(types, new CredentialTypeDefinition("oauth2-client", "OAuth2 Client", "OAuth2 client credentials",
                List.of(
                        field("clientId", "Client ID", CredentialFieldType.TEXT, true, false),
                        field("clientSecret", "Client Secret", CredentialFieldType.SECRET, true, true),
                        field("tokenUrl", "Token URL", CredentialFieldType.TEXT, true, false),
                        new CredentialFieldDefinition("scopes", "Scopes", CredentialFieldType.KEY_VALUE, false, false, true))));
        register(types, new CredentialTypeDefinition("client-certificate", "Client Certificate", "Certificate and private key",
                List.of(
                        field("certificate", "Certificate", CredentialFieldType.FILE, true, false),
                        field("privateKey", "Private Key", CredentialFieldType.FILE, true, true),
                        field("password", "Private Key Password", CredentialFieldType.SECRET, false, true))));
        register(types, new CredentialTypeDefinition("java-keystore", "Java Keystore", "PKCS12 or JKS keystore",
                List.of(
                        field("keystore", "Keystore", CredentialFieldType.FILE, true, true),
                        field("password", "Keystore Password", CredentialFieldType.SECRET, true, true),
                        field("type", "Keystore Type", CredentialFieldType.TEXT, true, false))));
        this.definitions = Map.copyOf(types);
    }

    /** @return all registered credential type definitions */
    public Collection<CredentialTypeDefinition> getAll() {
        return definitions.values();
    }

    /** @param key credential type key
     * @return matching definition, or an empty result when the key is unknown
     */
    public Optional<CredentialTypeDefinition> findByKey(String key) {
        return Optional.ofNullable(definitions.get(key));
    }

    /** @param key credential type key
     * @return matching definition
     * @throws IllegalArgumentException when the key is not registered
     */
    public CredentialTypeDefinition getRequired(String key) {
        return findByKey(key).orElseThrow(() -> new IllegalArgumentException("Unknown credential type: " + key));
    }

    private static void register(Map<String, CredentialTypeDefinition> types, CredentialTypeDefinition definition) {
        if (types.putIfAbsent(definition.key(), definition) != null) {
            throw new IllegalStateException("Duplicate credential type: " + definition.key());
        }
    }

    private static CredentialFieldDefinition field(
            String key, String label, CredentialFieldType type, boolean required, boolean sensitive) {
        return new CredentialFieldDefinition(key, label, type, required, sensitive, false);
    }
}

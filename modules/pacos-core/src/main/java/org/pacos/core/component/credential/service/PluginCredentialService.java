package org.pacos.core.component.credential.service;

import java.util.List;
import java.util.Objects;

import org.pacos.base.credential.CredentialDescriptor;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.base.credential.CredentialService;
import org.pacos.base.credential.ResolvedCredential;

/**
 * Plugin-scoped read-only credential facade.
 *
 * <p>The plugin identity is supplied by the host during plugin-context creation and cannot be
 * selected or overridden by plugin code. Administrative metadata is reduced to the public
 * non-secret descriptor before it crosses the plugin API boundary.</p>
 */
public final class PluginCredentialService implements CredentialService {

    private final CredentialVaultService credentialVaultService;
    private final CredentialAccessContext accessContext;

    public PluginCredentialService(CredentialVaultService credentialVaultService, String pluginId) {
        this.credentialVaultService = Objects.requireNonNull(credentialVaultService);
        this.accessContext = CredentialAccessContext.forPlugin(pluginId);
    }

    @Override
    public List<CredentialDescriptor> findAvailable(CredentialQuery query) {
        return credentialVaultService.findAvailable(query, accessContext).stream()
                .map(PluginCredentialService::toPluginDescriptor)
                .toList();
    }

    @Override
    public CredentialDescriptor findById(CredentialReference reference) {
        return toPluginDescriptor(credentialVaultService.findById(reference, accessContext));
    }

    @Override
    public ResolvedCredential resolve(CredentialReference reference) {
        return credentialVaultService.resolve(reference, accessContext);
    }

    private static CredentialDescriptor toPluginDescriptor(CredentialAdminDescriptor descriptor) {
        return new CredentialDescriptor(
                descriptor.id(),
                descriptor.name(),
                descriptor.description(),
                descriptor.type(),
                descriptor.configuredFields(),
                descriptor.configuredFiles());
    }
}

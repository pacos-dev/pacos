package org.pacos.core.component.credential.service;

import java.util.List;
import java.util.Objects;

import org.pacos.base.credential.CredentialDescriptor;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.base.credential.CredentialService;
import org.pacos.base.credential.ResolvedCredential;
import org.springframework.stereotype.Service;

/** Application-scoped read-only credential facade for PacOS components running in the root context. */
@Service
public final class ApplicationCredentialService implements CredentialService {

    private final CredentialVaultService credentialVaultService;
    private final CredentialAccessContext accessContext = CredentialAccessContext.forApplication();

    public ApplicationCredentialService(CredentialVaultService credentialVaultService) {
        this.credentialVaultService = Objects.requireNonNull(credentialVaultService);
    }

    @Override
    public List<CredentialDescriptor> findAvailable(CredentialQuery query) {
        return credentialVaultService.findAvailable(query, accessContext).stream()
                .map(ApplicationCredentialService::toApplicationDescriptor)
                .toList();
    }

    @Override
    public CredentialDescriptor findById(CredentialReference reference) {
        return toApplicationDescriptor(credentialVaultService.findById(reference, accessContext));
    }

    @Override
    public ResolvedCredential resolve(CredentialReference reference) {
        return credentialVaultService.resolve(reference, accessContext);
    }

    private static CredentialDescriptor toApplicationDescriptor(CredentialAdminDescriptor descriptor) {
        return new CredentialDescriptor(descriptor.id(), descriptor.name(), descriptor.description(),
                descriptor.type(), descriptor.configuredFields(), descriptor.configuredFiles());
    }
}

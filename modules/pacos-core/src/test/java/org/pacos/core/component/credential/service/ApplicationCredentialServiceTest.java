package org.pacos.core.component.credential.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.pacos.base.credential.CredentialDescriptor;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.base.credential.ResolvedCredential;

class ApplicationCredentialServiceTest {

    @Test
    void whenFindAvailableThenUseApplicationIdentityAndExposePublicMetadata() {
        CredentialVaultService vaultService = mock(CredentialVaultService.class);
        CredentialAdminDescriptor descriptor = adminDescriptor();
        when(vaultService.findAvailable(any(), any())).thenReturn(List.of(descriptor));
        ApplicationCredentialService service = new ApplicationCredentialService(vaultService);

        List<CredentialDescriptor> result = service.findAvailable(CredentialQuery.all());

        assertEquals(List.of(new CredentialDescriptor(descriptor.id(), descriptor.name(), descriptor.description(),
                descriptor.type(), descriptor.configuredFields(), descriptor.configuredFiles())), result);
        verify(vaultService).findAvailable(CredentialQuery.all(), CredentialAccessContext.forApplication());
    }

    @Test
    void whenFindByIdThenUseApplicationIdentity() {
        CredentialVaultService vaultService = mock(CredentialVaultService.class);
        CredentialReference reference = new CredentialReference(UUID.randomUUID());
        CredentialAdminDescriptor descriptor = adminDescriptor();
        when(vaultService.findById(reference, CredentialAccessContext.forApplication())).thenReturn(descriptor);
        ApplicationCredentialService service = new ApplicationCredentialService(vaultService);

        assertEquals(descriptor.id(), service.findById(reference).id());
    }

    @Test
    void whenResolveThenUseApplicationIdentity() {
        CredentialVaultService vaultService = mock(CredentialVaultService.class);
        CredentialReference reference = new CredentialReference(UUID.randomUUID());
        ResolvedCredential resolved = mock(ResolvedCredential.class);
        when(vaultService.resolve(reference, CredentialAccessContext.forApplication())).thenReturn(resolved);
        ApplicationCredentialService service = new ApplicationCredentialService(vaultService);

        assertSame(resolved, service.resolve(reference));
    }

    private static CredentialAdminDescriptor adminDescriptor() {
        return new CredentialAdminDescriptor(UUID.randomUUID(), "Application API", "Application credentials",
                "bearer-token", 7, CredentialVisibility.PRIVATE, Instant.now(), Instant.now(),
                Set.of("token"), Set.of(), false);
    }
}

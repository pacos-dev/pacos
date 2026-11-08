package org.pacos.core.component.credential.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.pacos.base.credential.CredentialDescriptor;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.base.credential.ResolvedCredential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class PluginCredentialServiceTest {

    @Test
    void whenFindAvailableThenUseBoundPluginIdentityAndExposeOnlyPluginMetadata() {
        CredentialVaultService vaultService = mock(CredentialVaultService.class);
        CredentialAdminDescriptor adminDescriptor = adminDescriptor();
        when(vaultService.findAvailable(any(), any())).thenReturn(List.of(adminDescriptor));
        PluginCredentialService service = new PluginCredentialService(vaultService, "apinity");

        List<CredentialDescriptor> result = service.findAvailable(new CredentialQuery(null, null));

        assertEquals(List.of(pluginDescriptor(adminDescriptor)), result);
        verify(vaultService).findAvailable(new CredentialQuery(null, null),
                CredentialAccessContext.forPlugin("apinity"));
    }

    @Test
    void whenFindByIdThenUseBoundPluginIdentityAndExposeOnlyPluginMetadata() {
        CredentialVaultService vaultService = mock(CredentialVaultService.class);
        CredentialReference reference = new CredentialReference(UUID.randomUUID());
        CredentialAdminDescriptor adminDescriptor = adminDescriptor();
        when(vaultService.findById(reference, CredentialAccessContext.forPlugin("apinity")))
                .thenReturn(adminDescriptor);
        PluginCredentialService service = new PluginCredentialService(vaultService, "apinity");

        assertEquals(pluginDescriptor(adminDescriptor), service.findById(reference));
    }

    @Test
    void whenResolveThenUseBoundPluginIdentity() {
        CredentialVaultService vaultService = mock(CredentialVaultService.class);
        CredentialReference reference = new CredentialReference(UUID.randomUUID());
        ResolvedCredential resolved = mock(ResolvedCredential.class);
        when(vaultService.resolve(reference, CredentialAccessContext.forPlugin("apinity")))
                .thenReturn(resolved);
        PluginCredentialService service = new PluginCredentialService(vaultService, "apinity");

        assertSame(resolved, service.resolve(reference));
    }

    private static CredentialAdminDescriptor adminDescriptor() {
        return new CredentialAdminDescriptor(
                UUID.randomUUID(), "Production API", "Production credentials", "bearer-token",
                7, CredentialVisibility.PRIVATE, Instant.now(), Instant.now(),
                Set.of("token"), Set.of(), true);
    }

    private static CredentialDescriptor pluginDescriptor(CredentialAdminDescriptor descriptor) {
        return new CredentialDescriptor(
                descriptor.id(), descriptor.name(), descriptor.description(), descriptor.type(),
                descriptor.configuredFields(), descriptor.configuredFiles());
    }
}

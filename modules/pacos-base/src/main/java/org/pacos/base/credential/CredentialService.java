package org.pacos.base.credential;

import java.util.List;

public interface CredentialService {

    List<CredentialDescriptor> findAvailable(CredentialQuery query, CredentialAccessContext context);

    CredentialDescriptor findById(CredentialReference reference, CredentialAccessContext context);

    CredentialDescriptor create(CredentialCreateRequest request, CredentialAccessContext context);

    CredentialDescriptor update(CredentialReference reference, CredentialUpdateRequest request,
                                CredentialAccessContext context);

    void delete(CredentialReference reference, CredentialAccessContext context);

    List<CredentialAccessDescriptor> findAccess(CredentialReference reference, CredentialAccessContext context);

    void grantAccess(CredentialReference reference, CredentialAccessGrant grant, CredentialAccessContext context);

    void revokeAccess(CredentialReference reference, CredentialAccessGrant grant, CredentialAccessContext context);

    ResolvedCredential resolve(CredentialReference reference, CredentialAccessContext context);
}

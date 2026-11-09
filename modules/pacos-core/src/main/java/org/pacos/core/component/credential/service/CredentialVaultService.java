package org.pacos.core.component.credential.service;

import java.util.List;
import java.util.Map;

import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.base.credential.ResolvedCredential;

/**
 * Internal credential application service used by PacOS core.
 *
 * <p>This interface includes administrative operations and explicit authorization contexts. It is
 * not a plugin contract; plugin contexts receive only the read-only {@code CredentialService} facade.</p>
 */
public interface CredentialVaultService {

    /** Returns the credential type definitions registered by core. */
    List<CredentialTypeDefinition> getAvailableTypes();

    /**
     * Finds credentials visible to the supplied user, plugin or application principal.
     * @param query optional type and text filters
     * @param context trusted core-created authorization identity
     * @return descriptors containing core-only administrative metadata
     */
    List<CredentialAdminDescriptor> findAvailable(CredentialQuery query, CredentialAccessContext context);

    /**
     * Reads non-sensitive values for credential editing.
     * @param reference credential identifier
     * @param context authenticated user context with MANAGE permission
     * @return non-sensitive values; SECRET and FILE fields are excluded
     */
    Map<String, Object> findNonSensitiveValues(CredentialReference reference, CredentialAccessContext context);

    /**
     * Finds a credential and its administrative metadata.
     * @param reference credential identifier
     * @param context trusted core-created authorization identity
     * @return credential descriptor
     */
    CredentialAdminDescriptor findById(CredentialReference reference, CredentialAccessContext context);

    /**
     * Creates a credential for the authenticated owner.
     * @param request core-only creation payload
     * @param context authenticated non-guest user context
     * @return descriptor of the created credential
     */
    CredentialAdminDescriptor create(CredentialCreateRequest request, CredentialAccessContext context);

    /**
     * Applies a partial metadata, value and file update.
     * @param reference credential identifier
     * @param request core-only update payload
     * @param context authenticated user context with MANAGE permission
     * @return descriptor of the updated credential
     */
    CredentialAdminDescriptor update(CredentialReference reference, CredentialUpdateRequest request,
                                CredentialAccessContext context);

    /**
     * Deletes a credential and schedules encrypted-file cleanup.
     * @param reference credential identifier
     * @param context authenticated user context with MANAGE permission
     */
    void delete(CredentialReference reference, CredentialAccessContext context);

    /**
     * Lists ACL grants for a credential.
     * @param reference credential identifier
     * @param context authenticated user context with MANAGE permission
     * @return current group, plugin and application grants
     */
    List<CredentialAccessDescriptor> findAccess(CredentialReference reference, CredentialAccessContext context);

    /**
     * Adds an ACL grant. Plugins and the GUEST group may receive ACCESS only.
     * @param reference credential identifier
     * @param grant principal and permission to grant
     * @param context authenticated user context with MANAGE permission
     */
    void grantAccess(CredentialReference reference, CredentialAccessGrant grant, CredentialAccessContext context);

    /**
     * Removes an ACL grant.
     * @param reference credential identifier
     * @param grant principal and permission to revoke
     * @param context authenticated user context with MANAGE permission
     */
    void revokeAccess(CredentialReference reference, CredentialAccessGrant grant, CredentialAccessContext context);

    /**
     * Resolves decrypted values and files for an authorized principal.
     * @param reference credential identifier
     * @param context trusted core-created authorization identity
     * @return closeable sensitive-data view; caller must close it
     */
    ResolvedCredential resolve(CredentialReference reference, CredentialAccessContext context);
}

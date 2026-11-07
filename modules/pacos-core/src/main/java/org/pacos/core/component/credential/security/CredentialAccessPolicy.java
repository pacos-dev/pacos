package org.pacos.core.component.credential.security;

import org.pacos.base.credential.CredentialAccessContext;
import org.pacos.base.credential.CredentialAclPermission;
import org.pacos.base.credential.CredentialPrincipalType;
import org.pacos.base.credential.CredentialVisibility;
import org.pacos.base.security.PermissionName;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.repository.CredentialAccessRepository;
import org.pacos.core.component.security.service.PermissionService;
import org.springframework.stereotype.Component;

/**
 * Evaluates global permissions and credential-specific access grants.
 */
@Component
public class CredentialAccessPolicy {

    private final CredentialAccessRepository accessRepository;
    private final PermissionService permissionService;

    /**
     * Creates the access policy.
     * @param accessRepository repository for credential-specific grants
     * @param permissionService service for global permissions
     */
    public CredentialAccessPolicy(CredentialAccessRepository accessRepository, PermissionService permissionService) {
        this.accessRepository = accessRepository;
        this.permissionService = permissionService;
    }

    /**
     * Determines whether a user or plugin may inspect credential metadata.
     * @param credential credential being accessed
     * @param context caller identity and plugin context
     * @return whether metadata is visible
     */
    public boolean canView(CredentialEntity credential, CredentialAccessContext context) {
        if (context == null || context.userId() != null && context.userId() == UserDTO.GUEST_ID) {
            return false;
        }
        if (context.pluginId() != null) {
            if (context.userId() != null && !hasGlobalPermission(context.userId(), CredentialPermission.VIEW)) {
                return false;
            }
            return hasPluginAccess(credential, context.pluginId(), CredentialAclPermission.VIEW)
                    || hasPluginAccess(credential, context.pluginId(), CredentialAclPermission.USE)
                    || hasPluginAccess(credential, context.pluginId(), CredentialAclPermission.MANAGE);
        }
        if (context.userId() == null) {
            return false;
        }
        int userId = context.userId();
        return isOwner(credential, userId)
                || isAdmin(userId)
                || hasUserAccess(credential, userId, CredentialAclPermission.VIEW)
                || hasUserAccess(credential, userId, CredentialAclPermission.USE)
                || hasUserAccess(credential, userId, CredentialAclPermission.MANAGE)
                || credential.getVisibility() == CredentialVisibility.SHARED
                && hasGlobalPermission(userId, CredentialPermission.VIEW);
    }

    /**
     * Determines whether a user or plugin may resolve credential values.
     * @param credential credential being accessed
     * @param context caller identity and plugin context
     * @return whether credential values may be used
     */
    public boolean canUse(CredentialEntity credential, CredentialAccessContext context) {
        if (context == null || context.userId() != null && context.userId() == UserDTO.GUEST_ID) {
            return false;
        }
        if (context.userId() != null && !hasGlobalPermission(context.userId(), CredentialPermission.USE)) {
            return false;
        }
        if (context.pluginId() != null) {
            return hasPluginAccess(credential, context.pluginId(), CredentialAclPermission.USE)
                    || hasPluginAccess(credential, context.pluginId(), CredentialAclPermission.MANAGE);
        }
        if (context.userId() == null) {
            return false;
        }
        int userId = context.userId();
        return isOwner(credential, userId)
                || isAdmin(userId)
                || hasUserAccess(credential, userId, CredentialAclPermission.USE)
                || hasUserAccess(credential, userId, CredentialAclPermission.MANAGE)
                || credential.getVisibility() == CredentialVisibility.SHARED;
    }

    /**
     * Determines whether a user may manage credential metadata and grants.
     * @param credential credential being accessed
     * @param context caller identity and plugin context
     * @return whether the credential may be managed
     */
    public boolean canManage(CredentialEntity credential, CredentialAccessContext context) {
        if (context == null || context.userId() == null || context.userId() == UserDTO.GUEST_ID
                || context.pluginId() != null) {
            return false;
        }
        int userId = context.userId();
        return isOwner(credential, userId)
                || isAdmin(userId)
                || hasUserAccess(credential, userId, CredentialAclPermission.MANAGE)
                || hasGlobalPermission(userId, CredentialPermission.MANAGE_ACCESS);
    }

    /**
     * Checks a global Vault permission, including administrator access.
     * @param userId user identifier
     * @param permission permission to check
     * @return whether the user has the permission
     */
    public boolean hasGlobalPermission(int userId, CredentialPermission permission) {
        return isAdmin(userId) || permissionService.loadUserPermission(userId)
                .contains(new PermissionName(permission.getKey()));
    }

    private boolean hasUserAccess(CredentialEntity credential, int userId, CredentialAclPermission permission) {
        return accessRepository.hasAccess(
                credential.getId(), CredentialPrincipalType.USER, Integer.toString(userId), permission);
    }

    private boolean hasPluginAccess(CredentialEntity credential, String pluginId, CredentialAclPermission permission) {
        return accessRepository.hasAccess(
                credential.getId(), CredentialPrincipalType.PLUGIN, pluginId, permission);
    }

    private static boolean isOwner(CredentialEntity credential, int userId) {
        return credential.getOwnerUserId() == userId;
    }

    private static boolean isAdmin(int userId) {
        return userId == UserDTO.ADMIN_ID;
    }
}

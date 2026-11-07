package org.pacos.core.component.credential.security;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public CredentialViewAccess loadViewAccess(CredentialAccessContext context) {
        if (context == null || context.userId() != null && context.userId() == UserDTO.GUEST_ID) {
            return new CredentialViewAccess(context, Set.of(), false);
        }
        if (context.pluginId() != null) {
            if (context.userId() != null && !hasGlobalPermission(context.userId(), CredentialPermission.VIEW)) {
                return new CredentialViewAccess(context, Set.of(), false);
            }
            return new CredentialViewAccess(
                    context, loadCredentialIds(CredentialPrincipalType.PLUGIN, context.pluginId()), false);
        }
        if (context.userId() == null) {
            return new CredentialViewAccess(context, Set.of(), false);
        }
        int userId = context.userId();
        return new CredentialViewAccess(
                context,
                loadCredentialIds(CredentialPrincipalType.USER, Integer.toString(userId)),
                hasGlobalPermission(userId, CredentialPermission.VIEW));
    }

    public boolean canView(
            CredentialEntity credential, CredentialAccessContext context, CredentialViewAccess viewAccess) {
        if (context == null || viewAccess == null || !viewAccess.matches(context)
                || context.userId() != null && context.userId() == UserDTO.GUEST_ID) {
            return false;
        }
        if (context.pluginId() != null) {
            return viewAccess.hasCredentialAccess(credential.getId());
        }
        if (context.userId() == null) {
            return false;
        }
        int userId = context.userId();
        return isOwner(credential, userId)
                || isAdmin(userId)
                || viewAccess.hasCredentialAccess(credential.getId())
                || credential.getVisibility() == CredentialVisibility.SHARED && viewAccess.sharedVisible();
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
     * Determines whether a user may manage credential metadata.
     * @param credential credential being accessed
     * @param context caller identity and plugin context
     * @return whether the credential metadata may be managed
     */
    public boolean canManage(CredentialEntity credential, CredentialAccessContext context) {
        if (context == null || context.userId() == null || context.userId() == UserDTO.GUEST_ID
                || context.pluginId() != null) {
            return false;
        }
        int userId = context.userId();
        return isOwner(credential, userId)
                || isAdmin(userId)
                || hasUserAccess(credential, userId, CredentialAclPermission.MANAGE);
    }

    /**
     * Determines whether a user may administer access grants for a credential.
     * @param credential credential being accessed
     * @param context caller identity and plugin context
     * @return whether access grants may be administered
     */
    public boolean canManageAccess(CredentialEntity credential, CredentialAccessContext context) {
        return canManage(credential, context)
                || context != null && context.userId() != null
                && context.userId() != UserDTO.GUEST_ID && context.pluginId() == null
                && hasGlobalPermission(context.userId(), CredentialPermission.MANAGE_ACCESS);
    }

    private Set<String> loadCredentialIds(CredentialPrincipalType principalType, String principalId) {
        return accessRepository.findAllByPrincipalTypeAndPrincipalIdAndPermissionIn(
                        principalType, principalId, EnumSet.allOf(CredentialAclPermission.class))
                .stream()
                .map(access -> access.getCredential().getId())
                .collect(Collectors.toSet());
    }

    public static final class CredentialViewAccess {

        private final Integer userId;
        private final String pluginId;
        private final Set<String> credentialIds;
        private final boolean sharedVisible;

        private CredentialViewAccess(
                CredentialAccessContext context, Set<String> credentialIds, boolean sharedVisible) {
            this.userId = context == null ? null : context.userId();
            this.pluginId = context == null ? null : context.pluginId();
            this.credentialIds = Set.copyOf(credentialIds);
            this.sharedVisible = sharedVisible;
        }

        private boolean matches(CredentialAccessContext context) {
            return Objects.equals(userId, context.userId()) && Objects.equals(pluginId, context.pluginId());
        }

        private boolean hasCredentialAccess(String credentialId) {
            return credentialIds.contains(credentialId);
        }

        private boolean sharedVisible() {
            return sharedVisible;
        }
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

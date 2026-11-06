package org.pacos.core.component.credential.security;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAclPermission;
import org.pacos.core.component.credential.service.CredentialPrincipalType;
import org.pacos.core.component.credential.service.CredentialVisibility;
import org.pacos.base.session.UserDTO;
import org.pacos.core.component.credential.domain.CredentialAccessEntity;
import org.pacos.core.component.credential.domain.CredentialEntity;
import org.pacos.core.component.credential.repository.CredentialAccessRepository;
import org.pacos.core.component.user.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Evaluates credential-specific access grants for users and plugins.
 *
 * <p>Guest sessions participate in group-based access checks through their assigned GUEST role.
 * A guest can view and use a credential only when that role has an {@link CredentialAclPermission#ACCESS}
 * grant (or when the credential is otherwise visible under the normal visibility rules).
 * Guest sessions can never manage credentials or access grants, even if the GUEST role has a
 * {@link CredentialAclPermission#MANAGE} grant.</p>
 *
 * <p>When an access context contains a plugin ID, the plugin is the authorization principal:
 * only that plugin's {@link CredentialAclPermission#ACCESS} grant is considered. The user's
 * group grants, including the GUEST role, do not grant access to a plugin execution context.</p>
 */
@Component
public class CredentialAccessPolicy {

    private static final Set<CredentialAclPermission> GROUP_ACCESS_PERMISSIONS =
            EnumSet.of(CredentialAclPermission.ACCESS, CredentialAclPermission.MANAGE);
    private static final Set<CredentialAclPermission> GROUP_MANAGE_PERMISSIONS =
            EnumSet.of(CredentialAclPermission.MANAGE);
    private static final Set<CredentialAclPermission> PLUGIN_ACCESS_PERMISSIONS =
            EnumSet.of(CredentialAclPermission.ACCESS);

    private final CredentialAccessRepository accessRepository;
    private final UserRepository userRepository;

    public CredentialAccessPolicy(
            CredentialAccessRepository accessRepository,
            UserRepository userRepository) {
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
    }

    /**
     * Checks whether a user or plugin can view credential metadata.
     *
     * <p>Guest users are evaluated against their assigned groups in the same way as other users.
     * Plugin contexts are evaluated only against the plugin's own ACCESS grant.</p>
     *
     * @param credential credential to check
     * @param context user or plugin access context
     * @return whether metadata is visible
     */
    public boolean canView(CredentialEntity credential, CredentialAccessContext context) {
        if (context == null) {
            return false;
        }
        if (context.pluginId() != null) {
            return hasPluginAccess(credential, context.pluginId());
        }
        if (context.userId() == null) {
            return false;
        }
        int userId = context.userId();
        return isOwner(credential, userId)
                || isAdmin(userId)
                || hasGroupAccess(credential, userId, GROUP_ACCESS_PERMISSIONS)
                || credential.getVisibility() == CredentialVisibility.SHARED;
    }

    /**
     * Loads the grants used by the bulk credential listing operation.
     *
     * <p>For guest users this includes grants assigned to the GUEST role. Plugin contexts load
     * only plugin grants and never inherit the associated user's group grants.</p>
     *
     * @param context user or plugin access context
     * @return an immutable snapshot of the grants relevant to the context
     */
    public CredentialViewAccess loadViewAccess(CredentialAccessContext context) {
        if (context == null) {
            return new CredentialViewAccess(null, Set.of(), Set.of(), false);
        }
        if (context.pluginId() != null) {
            return new CredentialViewAccess(
                    context,
                    loadCredentialIds(CredentialPrincipalType.PLUGIN, context.pluginId(), PLUGIN_ACCESS_PERMISSIONS),
                    Set.of(),
                    false);
        }
        if (context.userId() == null) {
            return new CredentialViewAccess(context, Set.of(), Set.of(), false);
        }
        int userId = context.userId();
        Set<String> credentialIds = new HashSet<>();
        Set<String> manageableCredentialIds = new HashSet<>();
        List<String> groupIds = getUserRoleIds(userId);
        if (!groupIds.isEmpty()) {
            List<CredentialAccessEntity> grants = accessRepository
                    .findAllByPrincipalTypeAndPrincipalIdInAndPermissionIn(
                            CredentialPrincipalType.GROUP, groupIds, GROUP_ACCESS_PERMISSIONS);
            credentialIds.addAll(grants.stream()
                    .map(access -> access.getCredential().getId())
                    .collect(Collectors.toSet()));
            manageableCredentialIds.addAll(grants.stream()
                    .filter(access -> access.getPermission() == CredentialAclPermission.MANAGE)
                    .map(access -> access.getCredential().getId())
                    .collect(Collectors.toSet()));
        }
        return new CredentialViewAccess(context, credentialIds, manageableCredentialIds, true);
    }

    /**
     * Checks credential visibility against a previously loaded bulk-access snapshot.
     *
     * <p>The same guest and plugin principal rules as {@link #canView(CredentialEntity, CredentialAccessContext)}
     * apply. The snapshot must belong to the supplied access context.</p>
     *
     * @param credential credential to check
     * @param context user or plugin access context
     * @param viewAccess previously loaded access snapshot
     * @return whether metadata is visible
     */
    public boolean canView(
            CredentialEntity credential, CredentialAccessContext context, CredentialViewAccess viewAccess) {
        if (context == null || viewAccess == null || !viewAccess.matches(context)) {
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
     * Checks whether a user or plugin can resolve and use the credential's values.
     *
     * <p>Guest sessions may use values when the GUEST role has ACCESS. Plugin execution requires
     * an ACCESS grant for the plugin principal.</p>
     *
     * @param credential credential to use
     * @param context user or plugin access context
     * @return whether the credential can be used
     */
    public boolean canUse(CredentialEntity credential, CredentialAccessContext context) {
        return canView(credential, context);
    }

    /**
     * Checks whether a user can change a credential or its access grants.
     *
     * <p>Guest sessions and plugin contexts are always denied. A MANAGE grant assigned to the
     * GUEST role does not override this restriction.</p>
     *
     * @param credential credential to manage
     * @param context user or plugin access context
     * @return whether the credential can be managed
     */
    public boolean canManage(CredentialEntity credential, CredentialAccessContext context) {
        if (context == null || context.userId() == null || context.userId() == UserDTO.GUEST_ID
                || context.pluginId() != null) {
            return false;
        }
        int userId = context.userId();
        return isOwner(credential, userId)
                || isAdmin(userId)
                || hasGroupAccess(credential, userId, GROUP_MANAGE_PERMISSIONS);
    }

    /**
     * Checks whether a credential is manageable using a previously loaded access snapshot.
     *
     * @param credential credential to check
     * @param context user or plugin access context
     * @param viewAccess snapshot loaded for the same context
     * @return whether the context can manage the credential
     */
    public boolean canManage(
            CredentialEntity credential, CredentialAccessContext context, CredentialViewAccess viewAccess) {
        if (context == null || viewAccess == null || !viewAccess.matches(context)
                || context.userId() == null || context.userId() == UserDTO.GUEST_ID
                || context.pluginId() != null) {
            return false;
        }
        int userId = context.userId();
        return isOwner(credential, userId)
                || isAdmin(userId)
                || viewAccess.hasManageAccess(credential.getId());
    }

    private Set<String> loadCredentialIds(
            CredentialPrincipalType principalType, String principalId, Set<CredentialAclPermission> permissions) {
        return accessRepository.findAllByPrincipalTypeAndPrincipalIdAndPermissionIn(
                        principalType, principalId, permissions)
                .stream()
                .map(access -> access.getCredential().getId())
                .collect(Collectors.toSet());
    }

    private boolean hasGroupAccess(
            CredentialEntity credential, int userId, Set<CredentialAclPermission> permissions) {
        List<String> groupIds = getUserRoleIds(userId);
        return !groupIds.isEmpty() && accessRepository.hasAccessForPrincipals(
                credential.getId(), CredentialPrincipalType.GROUP, groupIds, permissions);
    }

    private List<String> getUserRoleIds(int userId) {
        List<Integer> roleIds = userRepository.findRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleIds.stream().map(String::valueOf).toList();
    }

    public static final class CredentialViewAccess {

        private final Integer userId;
        private final String pluginId;
        private final Set<String> credentialIds;
        private final Set<String> manageableCredentialIds;
        private final boolean sharedVisible;

        private CredentialViewAccess(
                CredentialAccessContext context,
                Set<String> credentialIds,
                Set<String> manageableCredentialIds,
                boolean sharedVisible) {
            this.userId = context == null ? null : context.userId();
            this.pluginId = context == null ? null : context.pluginId();
            this.credentialIds = Set.copyOf(credentialIds);
            this.manageableCredentialIds = Set.copyOf(manageableCredentialIds);
            this.sharedVisible = sharedVisible;
        }

        private boolean matches(CredentialAccessContext context) {
            return Objects.equals(userId, context.userId()) && Objects.equals(pluginId, context.pluginId());
        }

        private boolean hasCredentialAccess(String credentialId) {
            return credentialIds.contains(credentialId);
        }

        private boolean hasManageAccess(String credentialId) {
            return manageableCredentialIds.contains(credentialId);
        }

        private boolean sharedVisible() {
            return sharedVisible;
        }
    }

    private boolean hasPluginAccess(CredentialEntity credential, String pluginId) {
        return accessRepository.hasAccess(
                credential.getId(), CredentialPrincipalType.PLUGIN, pluginId, CredentialAclPermission.ACCESS);
    }

    private static boolean isOwner(CredentialEntity credential, int userId) {
        return credential.getOwnerUserId() == userId;
    }

    private static boolean isAdmin(int userId) {
        return userId == UserDTO.ADMIN_ID;
    }
}

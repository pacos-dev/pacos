package org.pacos.core.component.security.service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.pacos.base.security.Permission;
import org.pacos.base.security.PermissionName;
import org.pacos.core.component.security.domain.AppPermission;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.AppPermissionConfig;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.repository.PermissionRepository;
import org.pacos.core.component.security.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionService.class);
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public int resolvePermissionDefinition(Collection<Permission> incomingActions) {
        List<String> existingKeys = permissionRepository.findAll()
                .stream()
                .map(AppPermission::getKey)
                .toList();

        List<Permission> missing = incomingActions.stream()
                .filter(a -> !existingKeys.contains(a.getKey()))
                .toList();

        if (!missing.isEmpty()) {
            LOG.info("Adding missing action definition for {} actions", incomingActions.size());
            permissionRepository.saveAll(missing.stream().map(AppPermission::new).collect(Collectors.toSet()));
        }
        return missing.size();
    }

    @Transactional
    public List<AppPermissionConfig> loadPermissionsConfig(RoleDTO roleDTO) {
        Set<AppPermission> rolePermissions = roleRepository.findById(roleDTO.getId()).map(Role::getAppPermissions).orElse(Set.of());
        List<AppPermission> allPermissions = permissionRepository.findAllByOrderByCategoryAscKeyAsc();
        return allPermissions.stream().map(p ->
                        new AppPermissionConfig(p.getId(), p.getKey(), p.getLabel(), p.getCategory(), p.getDescription(), rolePermissions.contains(p)))
                .toList();
    }

    @Transactional
    public void savePermissionState(Integer permissionId, Boolean state, Integer roleId) {
        if (Boolean.TRUE.equals(state)) {
            permissionRepository.addPermissionForRole(permissionId, roleId);
        } else {
            permissionRepository.removePermissionFromRole(permissionId, roleId);
        }
    }

    public Set<PermissionName> loadUserPermission(int userId) {
        if (roleRepository.isUserHaveRootRole(userId, Role.ROOT_ROLE)) {
            return permissionRepository.findAll().stream()
                    .map(p -> new PermissionName(p.getKey())).collect(Collectors.toSet());
        }
        return permissionRepository.findAllByUserId(userId)
                .stream().filter(Objects::nonNull)
                .map(PermissionName::new)
                .collect(Collectors.toSet());
    }
}

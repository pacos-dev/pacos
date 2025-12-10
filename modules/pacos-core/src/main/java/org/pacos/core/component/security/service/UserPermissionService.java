package org.pacos.core.component.security.service;

import java.util.Optional;
import java.util.Set;

import org.pacos.base.security.PermissionConfig;
import org.pacos.base.session.AccessDecision;
import org.pacos.core.component.security.domain.UserPermission;
import org.pacos.core.component.security.dto.PermissionDetailConfig;
import org.pacos.core.component.security.repository.UserPermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPermissionService {

    private final UserPermissionRepository userPermissionRepository;

    public UserPermissionService(UserPermissionRepository userPermissionRepository) {
        this.userPermissionRepository = userPermissionRepository;
    }

    public Set<PermissionConfig> getUserPermissions(int userId) {
        return userPermissionRepository.findUserPermissions(userId);
    }


    public Set<PermissionDetailConfig> getUserPermissionsWithDetails(int userId) {
        return userPermissionRepository.findUserPermissionsDetail(userId);
    }

    @Transactional
    public void updateUserAction(Integer userId, PermissionDetailConfig perm, AccessDecision accessDecision) {
        Optional<UserPermission> permission = userPermissionRepository.findByUserIdAndKey(userId, perm.getKey());
        if (permission.isPresent()) {
            permission.get().setDecision(accessDecision);
            userPermissionRepository.save(permission.get());
        } else {
            userPermissionRepository.addNewPermission(perm.getKey(), userId, accessDecision.name());
        }
    }
}

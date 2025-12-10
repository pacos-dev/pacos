package org.pacos.core.component.security.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.pacos.core.component.security.domain.PermissionDefaultConfig;
import org.pacos.core.component.security.domain.Permissions;
import org.pacos.core.component.security.repository.PermissionDefaultConfigRepository;
import org.pacos.core.component.security.repository.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionService.class);
    private final PermissionRepository permissionRepository;
    private final PermissionDefaultConfigRepository actionDefRepository;

    public PermissionService(PermissionRepository permissionRepository,
            PermissionDefaultConfigRepository defaultConfigRepository) {
        this.permissionRepository = permissionRepository;
        this.actionDefRepository = defaultConfigRepository;
    }

    @Transactional
    public int resolvePermissionDefinition(Collection<org.pacos.base.security.Permission> incomingActions) {
        List<String> existingKeys = permissionRepository.findAll()
                .stream()
                .map(Permissions::getKey)
                .toList();

        List<org.pacos.base.security.Permission> missing = incomingActions.stream()
                .filter(a -> !existingKeys.contains(a.getKey()))
                .toList();

        if (!missing.isEmpty()) {
            LOG.info("Adding missing action definition for {} actions", incomingActions.size());
            List<Permissions> newActions =
                    permissionRepository.saveAll(missing.stream().map(Permissions::new).collect(Collectors.toSet()));
            actionDefRepository.saveAll(newActions.stream()
                    .map(PermissionDefaultConfig::new)
                    .collect(Collectors.toSet()));
        }
        return missing.size();
    }
}

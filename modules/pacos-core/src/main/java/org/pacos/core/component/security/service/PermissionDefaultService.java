package org.pacos.core.component.security.service;

import java.util.List;

import org.pacos.base.security.Permission;
import org.pacos.base.session.AccessDecision;
import org.pacos.core.component.security.dto.PermissionDetailConfig;
import org.pacos.core.component.security.repository.PermissionDefaultConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionDefaultService {

    private final PermissionDefaultConfigRepository defaultConfigRepository;

    public PermissionDefaultService(PermissionDefaultConfigRepository defaultConfigRepository) {

        this.defaultConfigRepository = defaultConfigRepository;
    }

    @Transactional
    public List<PermissionDetailConfig> findAllOrdered() {
        return defaultConfigRepository.findAllOrderByCategoryAndKey().stream().map(e ->
                new PermissionDetailConfig(
                        new Permission() {

                            @Override
                            public String getKey() {
                                return e.getAction().getKey();
                            }

                            @Override
                            public String getLabel() {
                                return e.getAction().getLabel();
                            }

                            @Override
                            public String getCategory() {
                                return e.getAction().getCategory();
                            }

                            @Override
                            public String getDescription() {
                                return e.getAction().getDescription();
                            }
                        },
                        e.getDecision())).toList();
    }

    @Transactional
    public void updateConfiguration(PermissionDetailConfig permissionConfig, boolean selected) {
        defaultConfigRepository.findById(permissionConfig.getKey()).ifPresent(config -> {
            config.setDecision(selected ? AccessDecision.ALLOW : AccessDecision.DENY);
            defaultConfigRepository.save(config);
        });
    }
}

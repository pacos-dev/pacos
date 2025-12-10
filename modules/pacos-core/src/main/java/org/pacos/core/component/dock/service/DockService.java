package org.pacos.core.component.dock.service;

import java.util.List;

import lombok.AllArgsConstructor;
import org.pacos.core.component.dock.domain.DockConfiguration;
import org.pacos.core.component.dock.repository.DockConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DockService {

    private final DockConfigurationRepository dockConfigurationRepository;

    public List<DockConfiguration> findByUserIdOrderByOrderNum(Integer userId) {
        return dockConfigurationRepository.findByUserIdOrderByOrderNum(userId);
    }

    public DockConfiguration addActivator(String activatorClassName, int userId) {
        DockConfiguration configuration = new DockConfiguration(activatorClassName,userId);
        return dockConfigurationRepository.save(configuration);
    }

    @Transactional("coreTransactionManager")
    public void removeActivator(String activatorClass, int userId) {
        dockConfigurationRepository.removeActivator(activatorClass, userId);
    }
}

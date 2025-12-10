package org.pacos.core.component.dock.proxy;

import java.util.List;

import lombok.AllArgsConstructor;
import org.pacos.core.component.dock.domain.DockConfiguration;
import org.pacos.core.component.dock.dto.DockConfigurationDTO;
import org.pacos.core.component.dock.service.DockService;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DockServiceProxy {

    private final DockService dockService;

    public List<DockConfigurationDTO> loadConfigurations(int userId) {
        return dockService.findByUserIdOrderByOrderNum(userId).stream().map(this::map).toList();
    }

    public DockConfigurationDTO addActivator(String activatorClassName, int userId) {
        return map(dockService.addActivator(activatorClassName, userId));
    }

    private DockConfigurationDTO map(DockConfiguration dockConfiguration) {
        DockConfigurationDTO configurationDTO = new DockConfigurationDTO(dockConfiguration.getId());
        configurationDTO.setUserId(dockConfiguration.getUserId());
        configurationDTO.setActivator(dockConfiguration.getActivatorClass());
        configurationDTO.setOrderNum(dockConfiguration.getOrderNum());
        return configurationDTO;
    }

    public void removeActivator(String simpleName, int userId) {
        dockService.removeActivator(simpleName, userId);
    }
}

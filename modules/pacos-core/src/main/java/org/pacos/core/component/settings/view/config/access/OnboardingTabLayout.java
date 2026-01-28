package org.pacos.core.component.settings.view.config.access;

import java.util.List;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.RoleService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;

@Component
@Scope("prototype")
public class OnboardingTabLayout extends SettingPageLayout {

    private static final String LABEL_HEADER = "Label";
    private static final String DESCRIPTION_HEADER = "Description";
    private static final String INFO = """
            Roles selected below will be automatically granted to all new users upon account creation
            """;
    private final List<Integer> onboardRoles;
    private final RegistryProxy registryProxy;

    public OnboardingTabLayout(RoleService roleService, RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
        add(new InfoBox(INFO));
        onboardRoles = registryProxy.readIntList(RegistryName.ONBOARD_ROLES);
        Grid<RoleDTO> roleDTOGrid = new Grid<>();
        roleDTOGrid.addComponentColumn(this::createCheckBoxBtn).setWidth("30px");
        roleDTOGrid.addColumn(RoleDTO::getLabel).setHeader(LABEL_HEADER).setAutoWidth(true).setResizable(true);
        roleDTOGrid.addColumn(RoleDTO::getDescription).setHeader(DESCRIPTION_HEADER).setAutoWidth(true).setResizable(true);
        add(roleDTOGrid);
        setSizeFull();
        addAttachListener(e -> roleDTOGrid.setItems(roleService.loadAllRoles()));
    }

    private Checkbox createCheckBoxBtn(RoleDTO roleDTO) {
        Checkbox cb = new Checkbox();
        cb.setValue(onboardRoles.contains(roleDTO.getId()));
        cb.addValueChangeListener(e ->
        {
            if (Boolean.TRUE.equals(e.getValue())) {
                onboardRoles.add(roleDTO.getId());
                NotificationUtils.success("Role '" + roleDTO.getLabel() + "' was added to the onboarding configuration");
            } else {
                onboardRoles.remove(roleDTO.getId());
                NotificationUtils.success("Role '" + roleDTO.getLabel() + "' was removed from the onboarding configuration");
            }
            updateRegistryValue();
        });
        return cb;
    }

    private void updateRegistryValue() {
        registryProxy.saveList(RegistryName.ONBOARD_ROLES, onboardRoles);
    }

    protected static String getSearchIndex() {
        return LABEL_HEADER + DESCRIPTION_HEADER + INFO;
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //not implemented
    }
}

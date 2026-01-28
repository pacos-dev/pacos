package org.pacos.core.component.settings.view.config.access;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.event.UISystem;
import org.pacos.base.exception.PacosException;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.PermissionService;
import org.pacos.core.component.security.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.binder.ValidationException;

@Component
@Scope("prototype")
public class RolesManagementTabView extends SettingPageLayout {

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final Grid<RoleDTO> gridRoles;
    private static final Logger LOG = LoggerFactory.getLogger(RolesManagementTabView.class);

    public RolesManagementTabView(RoleService roleService, PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.setSizeFull();
        add(new InfoBox("""
                Manage user roles and assign permissions to control access to system modules.
                """));

        this.gridRoles = new Grid<>();
        var labelColumn = this.gridRoles.addColumn(RoleDTO::getLabel).setHeader("Label").setResizable(true);
        this.gridRoles.addColumn(RoleDTO::getDescription).setHeader("Description").setResizable(true);
        this.gridRoles.addComponentColumn(this::btnBar).setWidth("160px");

        refreshItems();
        gridRoles.setSizeFull();
        Button addRole = new Button("Add new role", VaadinIcon.PLUS.create());
        addRole.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addRole.addClickListener(e -> showDialogWithFormEvent(null));
        this.gridRoles.appendFooterRow().getCell(labelColumn).setComponent(addRole);
        add(gridRoles);
    }

    private Div btnBar(RoleDTO roleDTO) {
        return DivUtils.ofClass("role-btn-bar")
                .withComponents(createEditButtonForRole(roleDTO), clonRolesButton(roleDTO), editPermissions(roleDTO), createRemoveButtonForRole(roleDTO));
    }

    private Button clonRolesButton(RoleDTO roleDTO) {
        return new ButtonUtils(VaadinIcon.COPY.create(), e -> cloneEvent(roleDTO))
                .withEnabled(!roleDTO.getId().equals(Role.ROOT_ROLE)).withTooltip("Clone role");
    }

    private void cloneEvent(RoleDTO roleDTO) {
        try {
            roleService.cloneRole(roleDTO);
            refreshItems();
        } catch (RuntimeException e) {
            NotificationUtils.error(e);
        }
    }

    private Button editPermissions(RoleDTO roleDTO) {
        Button btn = new Button(VaadinIcon.LOCK.create());
        btn.setTooltipText("Edit permissions");
        btn.setWidth(30, Unit.PIXELS);
        btn.setThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btn.setEnabled(!roleDTO.getId().equals(Role.ROOT_ROLE));
        btn.addClickListener(e -> {

            List<DesktopWindow> permWindows = UISystem.getCurrent()
                    .getWindowManager().getInitializedWindowsOfClass(PanelRolePermissionWindow.class);

            Optional<DesktopWindow> roleWindow = Optional.empty();
            if (permWindows != null) {
                roleWindow = permWindows.stream().filter(w -> ((PanelRolePermissionWindow) w).getRoleDTO().equals(roleDTO))
                        .findFirst();
            }
            if (roleWindow.isPresent()) {
                UISystem.getCurrent().getWindowManager().showAndMoveToFront(roleWindow.get());
            } else {
                RolePermissionWindowConfig permissionWindowConfig = new RolePermissionWindowConfig(roleDTO);
                PanelRolePermissionWindow window = new PanelRolePermissionWindow(permissionWindowConfig, roleDTO, permissionService);
                UISystem.getCurrent().getWindowManager().showWindow(window);
            }
        });
        return btn;
    }

    private Button createRemoveButtonForRole(RoleDTO roleDTO) {
        Button removeBtn = new ButtonUtils(VaadinIcon.TRASH.create(), e -> showConfirmationDialog(roleDTO));
        removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        if (Objects.equals(Role.ROOT_ROLE, roleDTO.getId()) || Objects.equals(Role.GUEST_ROLE, roleDTO.getId())) {
            removeBtn.setEnabled(false);
        }
        return removeBtn;
    }

    private ButtonUtils createEditButtonForRole(RoleDTO roledDto) {
        return new ButtonUtils(VaadinIcon.EDIT.create(), e -> showDialogWithFormEvent(roledDto))
                .withTooltip("Edit role entry");
    }

    private void refreshItems() {
        List<RoleDTO> roles = roleService.loadAllRoles();
        gridRoles.setItems(roles);
    }

    private void showConfirmationDialog(RoleDTO roleDTO) {
        int count = roleService.countRoleAssignment(roleDTO.getId());
        final ConfirmationWindowConfig config = new ConfirmationWindowConfig(() -> removeRoleEvent(roleDTO));
        config.setWarning(true);
        config.getWindowState().modal();
        config.setTitle("Remove role " + roleDTO.getLabel());
        config.getWindowState().withClosable(false);
        config.getWindowState().withConfirmationBtnLabel("Yes, remove");
        config.getWindowState().withWarningMode(true);
        config.setContent(VerticalLayoutUtils.defaults(
                new Paragraph("This role is assignment to '" + count + "' users."),
                new Paragraph("This operation can't be undone.")
        ));
        UISystem.getCurrent().getWindowManager().showModalWindow(config);
    }

    private boolean removeRoleEvent(RoleDTO roleDTO) {
        roleService.removeRole(roleDTO);
        refreshItems();
        return true;
    }

    private void showDialogWithFormEvent(RoleDTO roleDTO) {
        Dialog addDialog = new Dialog();
        addDialog.setCloseOnOutsideClick(false);
        addDialog.setHeaderTitle(roleDTO == null ? "Add new role" : "Edit role");
        RoleForm form = new RoleForm(roleDTO);
        addDialog.add(form);

        addDialog.getFooter().add(new ButtonUtils("Cancel", e -> addDialog.close()));
        addDialog.getFooter().add(new ButtonUtils("Save", e -> saveRole(form, addDialog)).primaryLayout());

        addDialog.open();
    }

    private void saveRole(RoleForm form, Dialog addDialog) {
        try {
            if (form.validate()) {
                RoleDTO roleDTO = form.getBean();
                roleService.createRole(roleDTO);
                refreshItems();
                addDialog.close();
            }
        } catch (ValidationException | PacosException e) {
            LOG.error("Error while saving role {}", e.getMessage(), e);
            NotificationUtils.error(e.getMessage());
        }
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //no detection
    }
}
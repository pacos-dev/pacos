package org.pacos.core.component.credential.view;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAccessDescriptor;
import org.pacos.core.component.credential.service.CredentialAccessGrant;
import org.pacos.core.component.credential.service.CredentialAclPermission;
import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.core.component.credential.service.CredentialPrincipalType;
import org.pacos.base.credential.CredentialReference;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.security.domain.Role;
import org.pacos.core.component.security.dto.RoleDTO;
import org.pacos.core.component.security.service.RoleService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Configures group and plugin grants for a credential.
 *
 * <p>The GUEST group is selectable and may receive ACCESS, allowing unauthenticated guest sessions
 * to use the credential. It is deliberately restricted to ACCESS; guests cannot manage credentials
 * or their grants. Plugin principals can also receive ACCESS only.</p>
 */
public class CredentialAccessDialog extends VerticalLayout {

    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;
    private static final String DUPLICATE_CONFIGURATION = "This configuration already exists.";

    private final CredentialVaultService credentialService;
    private final CredentialAccessContext accessContext;
    private final CredentialReference reference;
    private final Map<Integer, String> groupLabels;
    private final Grid<CredentialAccessDescriptor> grid = new Grid<>(CredentialAccessDescriptor.class, false);
    private final ComboBox<CredentialPrincipalType> principalType = new ComboBox<>("Access for");
    private final ComboBox<RoleDTO> group = new ComboBox<>("Group");
    private final ComboBox<PluginDTO> plugin = new ComboBox<>("Plugin");
    private final ComboBox<CredentialAclPermission> permission = new ComboBox<>("Permission");
    private List<CredentialAccessDescriptor> grants = List.of();

    public CredentialAccessDialog(
            CredentialVaultService credentialService,
            RoleService roleService,
            PluginProxy pluginProxy,
            CredentialAccessContext accessContext,
            CredentialAdminDescriptor descriptor) {
        this.credentialService = credentialService;
        this.accessContext = accessContext;
        this.reference = new CredentialReference(descriptor.id());
        List<RoleDTO> groups = roleService.loadAllRoles();
        this.groupLabels = groups.stream().collect(Collectors.toMap(RoleDTO::getId, RoleDTO::getLabel));

        setSizeFull();
        getStyle().set("overflow", "hidden");

        principalType.setItems(CredentialPrincipalType.GROUP, CredentialPrincipalType.PLUGIN);
        principalType.setValue(CredentialPrincipalType.GROUP);
        principalType.setWidth("10em");
        principalType.addValueChangeListener(event -> {
            clearGroupError();
            clearPluginError();
            updatePrincipalControls();
        });

        group.setItems(groups);
        group.setItemLabelGenerator(RoleDTO::getLabel);
        group.setWidth("16em");
        group.setPlaceholder("Select group");
        group.addValueChangeListener(event -> {
            clearGroupError();
            updatePermissionOptions();
        });

        plugin.setItems(pluginProxy.getPluginState().getPlugins());
        plugin.setItemLabelGenerator(this::pluginLabel);
        plugin.setWidth("18em");
        plugin.setPlaceholder("Select plugin");
        plugin.addValueChangeListener(event -> clearPluginError());

        permission.setWidth("10em");
        permission.addValueChangeListener(event -> {
            clearGroupError();
            clearPluginError();
        });
        updatePrincipalControls();

        Button grant = new Button("Grant access", event -> grantAccess());
        HorizontalLayout form = new HorizontalLayout(principalType, group, plugin, permission, grant);
        form.setWidthFull();
        form.setAlignItems(FlexComponent.Alignment.END);
        form.setFlexGrow(1, group, plugin);

        grid.addColumn(value -> value.principalType() == CredentialPrincipalType.GROUP ? "Group" : "Plugin")
                .setHeader("Type").setAutoWidth(true);
        grid.addColumn(this::principalLabel).setHeader("Principal").setFlexGrow(1);
        grid.addColumn(value -> value.permission().name()).setHeader("Permission").setAutoWidth(true);
        grid.addComponentColumn(value -> new Button("Revoke", event -> revokeAccess(value)))
                .setHeader("Actions").setAutoWidth(true);
        grid.setWidthFull();
        grid.setHeightFull();

        setPadding(false);
        setSpacing(true);
        setWidthFull();
        setFlexGrow(1, grid);
        add(form, grid);
        refresh();
    }

    private String principalLabel(CredentialAccessDescriptor descriptor) {
        if (descriptor.principalType() == CredentialPrincipalType.GROUP) {
            try {
                return groupLabels.getOrDefault(Integer.valueOf(descriptor.principalId()), descriptor.principalId());
            } catch (NumberFormatException ignored) {
                return descriptor.principalId();
            }
        }
        return descriptor.principalId();
    }

    private void updatePrincipalControls() {
        boolean groupSelected = principalType.getValue() == CredentialPrincipalType.GROUP;
        group.setVisible(groupSelected);
        group.setRequiredIndicatorVisible(groupSelected);
        plugin.setVisible(!groupSelected);
        plugin.setRequiredIndicatorVisible(!groupSelected);
        updatePermissionOptions();
    }

    private String pluginLabel(PluginDTO plugin) {
        return plugin.getName() + " (" + plugin.getArtifactName() + ")";
    }

    private void updatePermissionOptions() {
        boolean guestGroupSelected = group.getValue() != null
                && Integer.valueOf(Role.GUEST_ROLE).equals(group.getValue().getId());
        permission.setItems(principalType.getValue() == CredentialPrincipalType.GROUP && !guestGroupSelected
                ? List.of(CredentialAclPermission.ACCESS, CredentialAclPermission.MANAGE)
                : List.of(CredentialAclPermission.ACCESS));
        if (permission.getValue() != CredentialAclPermission.ACCESS) {
            permission.setValue(CredentialAclPermission.ACCESS);
        }
    }

    public ModalWindowConfig toWindowConfig(String credentialName) {
        ModalWindowConfig config = new ModalWindowConfig();
        config.setTitle("Access — " + credentialName);
        config.setContent(this);
        config.getWindowState()
                .withWidth(WINDOW_WIDTH)
                .withHeight(WINDOW_HEIGHT)
                .setCancellable(true);
        return config;
    }

    private void grantAccess() {
        try {
            CredentialPrincipalType type = principalType.getValue();
            String principalId;
            if (type == CredentialPrincipalType.GROUP) {
                if (group.getValue() == null) {
                    throw new IllegalArgumentException("Select a group");
                }
                principalId = group.getValue().getId().toString();
            } else {
                if (plugin.getValue() == null) {
                    throw new IllegalArgumentException("Select a plugin");
                }
                principalId = plugin.getValue().getArtifactName();
            }
            CredentialAclPermission selectedPermission = permission.getValue();
            if (grants.stream().anyMatch(grant -> grant.principalType() == type
                    && grant.principalId().equals(principalId)
                    && grant.permission() == selectedPermission)) {
                markDuplicate(type);
                return;
            }
            credentialService.grantAccess(reference, new CredentialAccessGrant(
                    type, principalId, selectedPermission), accessContext);
            group.clear();
            plugin.clear();
            clearGroupError();
            clearPluginError();
            refresh();
        } catch (IllegalArgumentException exception) {
            if (DUPLICATE_CONFIGURATION.equals(exception.getMessage())) {
                markDuplicate(principalType.getValue());
            } else {
                NotificationUtils.error(exception.getMessage());
            }
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception.getMessage());
        }
    }

    private void markDuplicate(CredentialPrincipalType type) {
        if (type == CredentialPrincipalType.GROUP) {
            group.setErrorMessage(DUPLICATE_CONFIGURATION);
            group.setInvalid(true);
        } else {
            plugin.setErrorMessage(DUPLICATE_CONFIGURATION);
            plugin.setInvalid(true);
        }
    }

    private void clearGroupError() {
        group.setInvalid(false);
        group.setErrorMessage(null);
    }

    private void clearPluginError() {
        plugin.setInvalid(false);
        plugin.setErrorMessage(null);
    }

    private void revokeAccess(CredentialAccessDescriptor grant) {
        try {
            credentialService.revokeAccess(reference, new CredentialAccessGrant(
                    grant.principalType(), grant.principalId(), grant.permission()), accessContext);
            refresh();
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception.getMessage());
        }
    }

    private void refresh() {
        try {
            grants = credentialService.findAccess(reference, accessContext);
            grid.setItems(grants);
        } catch (RuntimeException exception) {
            grants = List.of();
            NotificationUtils.error(exception.getMessage());
            grid.setItems(grants);
        }
    }
}

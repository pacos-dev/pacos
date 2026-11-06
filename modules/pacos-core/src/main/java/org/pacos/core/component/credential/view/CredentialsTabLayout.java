package org.pacos.core.component.credential.view;

import java.util.List;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.core.component.credential.service.CredentialAccessContext;
import org.pacos.core.component.credential.service.CredentialAdminDescriptor;
import org.pacos.base.credential.CredentialQuery;
import org.pacos.base.credential.CredentialReference;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.core.component.credential.service.CredentialTypeDefinition;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.security.service.RoleService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Displays credentials visible to the current session and exposes only actions allowed by each descriptor.
 *
 * <p>Visibility and manageability are calculated by {@link CredentialVaultService}; this view does not replace
 * the service's authorization checks.</p>
 */
public class CredentialsTabLayout extends SettingPageLayout {

    private static final String SEARCH = "Search credentials";
    private static final String TYPE = "Credential type";
    private static final String NAME = "Name";
    private static final String DESCRIPTION = "Description";
    private static final String OWNER = "Owner ID";
    private static final String VISIBILITY = "Visibility";
    private static final String ACTIONS = "Actions";

    private final CredentialVaultService credentialService;
    private final RoleService roleService;
    private final PluginProxy pluginProxy;
    private final CredentialAccessContext accessContext;
    private final UserSession userSession;
    private final long maxFileSizeBytes;
    private final List<CredentialTypeDefinition> credentialTypes;
    private final Grid<CredentialAdminDescriptor> grid = new Grid<>(CredentialAdminDescriptor.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<CredentialTypeDefinition> typeFilter = new ComboBox<>();
    private final HorizontalLayout toolbar = new HorizontalLayout();

    public CredentialsTabLayout(
            CredentialVaultService credentialService, RoleService roleService, PluginProxy pluginProxy,
            UserSession userSession, long maxFileSizeBytes) {
        this.credentialService = credentialService;
        this.roleService = roleService;
        this.pluginProxy = pluginProxy;
        this.userSession = userSession;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.credentialTypes = credentialService.getAvailableTypes();
        this.accessContext = CredentialAccessContext.forUser(userSession.getUserId());

        setSizeFull();
        configureFilters();
        configureGrid();

        Button createButton = new Button("Create credential", event -> openEditor(null));

        toolbar.add(searchField, typeFilter);
        if (userSession.getUserId() != UserDTO.GUEST_ID) {
            toolbar.add(createButton);
        }
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        searchField.setWidth("24em");
        typeFilter.setWidth("18em");

        add(toolbar, grid);
        refresh();
    }

    public static String getSearchIndex() {
        return "Credentials credential secret token password certificate keystore API key Basic Auth Bearer OAuth2 create edit delete";
    }

    private void configureFilters() {
        searchField.setPlaceholder(SEARCH);
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(event -> refresh());

        typeFilter.setLabel(TYPE);
        typeFilter.setClearButtonVisible(true);
        typeFilter.setItems(credentialTypes);
        typeFilter.setItemLabelGenerator(CredentialTypeDefinition::label);
        typeFilter.addValueChangeListener(event -> refresh());
    }

    private void configureGrid() {
        grid.addColumn(CredentialAdminDescriptor::name).setHeader(NAME).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(descriptor -> credentialTypes.stream()
                .filter(type -> type.key().equals(descriptor.type()))
                .map(CredentialTypeDefinition::label)
                .findFirst()
                .orElse(descriptor.type())).setHeader(TYPE).setAutoWidth(true);
        grid.addColumn(CredentialAdminDescriptor::ownerUserId).setHeader(OWNER).setAutoWidth(true);
        grid.addColumn(CredentialAdminDescriptor::description).setHeader(DESCRIPTION).setFlexGrow(2);
        grid.addColumn(descriptor -> descriptor.visibility().name()).setHeader(VISIBILITY).setAutoWidth(true);
        grid.addComponentColumn(this::createActions).setHeader(ACTIONS).setAutoWidth(true);
        grid.setSizeFull();
    }

    HorizontalLayout createActions(CredentialAdminDescriptor descriptor) {
        HorizontalLayout actions = new HorizontalLayout();
        if (!descriptor.manageable()) {
            return actions;
        }
        actions.add(new Button("Edit", event -> openEditor(descriptor)));
        actions.add(new Button("Access", event -> openAccess(descriptor)));
        Button delete = new ButtonUtils("Delete", VaadinIcon.TRASH.create(),
                event -> confirmDelete(descriptor)).errorLayout().withTooltip("Delete credential");
        delete.getElement().setAttribute("aria-label", "Delete credential");
        actions.add(delete);
        return actions;
    }

    private void openEditor(CredentialAdminDescriptor descriptor) {
        try {
            CredentialEditorDialog editor = new CredentialEditorDialog(
                    credentialService, accessContext, descriptor,
                    descriptor == null || descriptor.manageable(), maxFileSizeBytes, this::refresh);
            ModalWindowConfig config = editor.toWindowConfig();
            userSession.getUISystem().getWindowManager().showModalWindow(config);
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception.getMessage());
        }
    }

    private void openAccess(CredentialAdminDescriptor descriptor) {
        try {
            CredentialAccessDialog accessDialog = new CredentialAccessDialog(
                    credentialService, roleService, pluginProxy, accessContext, descriptor);
            ModalWindowConfig config = accessDialog.toWindowConfig(descriptor.name());
            userSession.getUISystem().getWindowManager().showModalWindow(config);
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception.getMessage());
        }
    }

    private void confirmDelete(CredentialAdminDescriptor descriptor) {
        try {
            ConfirmationWindowConfig config = createDeleteConfirmationConfig(descriptor);
            userSession.getUISystem().getWindowManager().showModalWindow(config);
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception.getMessage());
        }
    }

    ConfirmationWindowConfig createDeleteConfirmationConfig(CredentialAdminDescriptor descriptor) {
        ConfirmationWindowConfig config = new ConfirmationWindowConfig(() -> {
            try {
                credentialService.delete(new CredentialReference(descriptor.id()), accessContext);
                refresh();
                return true;
            } catch (RuntimeException exception) {
                NotificationUtils.error(exception.getMessage());
                return false;
            }
        });
        config.setWarning(true);
        config.setTitle("Delete credential");
        config.setContent(new VerticalLayout(
                new Span("Delete credential '" + descriptor.name() + "'? This action cannot be undone.")));
        config.getWindowState()
                .withWidth(440)
                .withHeight(180)
                .setCancellable(true)
                .withWarningMode(true);
        config.getWindowState().withConfirmationBtnLabel("Delete");
        return config;
    }

    void refresh() {
        try {
            CredentialTypeDefinition selectedType = typeFilter.getValue();
            String type = selectedType == null ? null : selectedType.key();
            grid.setItems(credentialService.findAvailable(
                    new CredentialQuery(type, searchField.getValue()), accessContext));
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception.getMessage());
            grid.setItems(java.util.List.of());
        }
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
    }
}

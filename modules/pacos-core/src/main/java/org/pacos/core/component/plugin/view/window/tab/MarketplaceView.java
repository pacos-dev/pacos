package org.pacos.core.component.plugin.view.window.tab;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.core.component.plugin.PluginPermissions;
import org.pacos.core.component.plugin.event.RefreshAvailablePluginEvent;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.view.plugin.PluginListView;
import org.pacos.core.component.repository.domain.PluginRepository;
import org.pacos.core.component.repository.service.PluginRepositoryService;

public class MarketplaceView extends VerticalLayout {

    private final PluginProxy pluginProxy;
    private final PluginRepositoryService repositoryService;
    private final ComboBox<PluginRepository> repositorySelector = new ComboBox<>("Plugin repository");
    private final Button addRepositoryButton = new Button("Add repository");
    private final Button removeRepositoryButton = new Button("Remove repository");
    private PluginListView pluginListView;
    private Long activeRepositoryId;

    public MarketplaceView(PluginProxy pluginProxy) {
        this.pluginProxy = pluginProxy;
        this.repositoryService = pluginProxy.getPluginRepositoryService();
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        repositorySelector.setItemLabelGenerator(PluginRepository::getName);
        repositorySelector.setWidthFull();
        addRepositoryButton.addClickListener(event -> openAddRepositoryDialog());
        removeRepositoryButton.addClickListener(event -> removeSelectedRepository());

        HorizontalLayout toolbar = new HorizontalLayout(repositorySelector, addRepositoryButton, removeRepositoryButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setFlexGrow(1, repositorySelector);
        add(toolbar);

        boolean canManageRepositories = hasInstallPermission();
        repositorySelector.setEnabled(canManageRepositories);
        addRepositoryButton.setEnabled(canManageRepositories);

        initializeRepositories();
        repositorySelector.addValueChangeListener(event -> onRepositorySelected(event.getValue()));
    }

    private void initializeRepositories() {
        List<PluginRepository> repositories = repositoryService.findAll();
        PluginRepository selected = repositoryService.getSelectedRepository();
        repositorySelector.setItems(repositories);
        repositorySelector.setValue(selected);
        showRepository(selected);
    }

    private void onRepositorySelected(PluginRepository selected) {
        if (selected == null || selected.getId() == null
                || selected.getId().equals(activeRepositoryId)) {
            return;
        }
        if (!requireRepositoryManagementPermission()) {
            repositorySelector.setValue(repositoryService.getSelectedRepository());
            return;
        }
        try {
            repositoryService.selectRepository(selected.getId());
            showRepository(selected);
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception.getMessage());
            repositorySelector.setValue(repositoryService.getSelectedRepository());
        }
    }

    private void showRepository(PluginRepository selected) {
        activeRepositoryId = selected.getId();
        removeRepositoryButton.setEnabled(hasInstallPermission()
                && repositoryService.findAll().size() > 1);
        AppRepository repository = repositoryService.toAppRepository(selected);
        if (pluginListView != null) {
            remove(pluginListView);
        }
        pluginListView = new PluginListView(pluginProxy, repository);
        add(pluginListView);
        setFlexGrow(1, pluginListView);
        RefreshAvailablePluginEvent.fireEvent(repository, this, pluginListView);
    }

    private void openAddRepositoryDialog() {
        if (!requireRepositoryManagementPermission()) {
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add plugin repository");
        TextField nameField = new TextField("Repository name");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setMaxLength(120);
        nameField.setWidthFull();

        TextField urlField = new TextField("Maven repository URL");
        urlField.setRequiredIndicatorVisible(true);
        urlField.setPlaceholder("https://repo.example.com/repository/plugins");
        urlField.setWidthFull();

        Button saveButton = new Button("Add", event -> {
            try {
                PluginRepository repository = repositoryService.addRepository(
                        nameField.getValue(), urlField.getValue());
                dialog.close();
                repositorySelector.setItems(repositoryService.findAll());
                activeRepositoryId = null;
                repositorySelector.setValue(repository);
            } catch (RuntimeException exception) {
                NotificationUtils.error(exception.getMessage());
            }
        });
        Button cancelButton = new Button("Cancel", event -> dialog.close());
        dialog.add(new VerticalLayout(nameField, urlField, new HorizontalLayout(saveButton, cancelButton)));
        dialog.open();
    }

    private void removeSelectedRepository() {
        if (!requireRepositoryManagementPermission()) {
            return;
        }
        PluginRepository selected = repositorySelector.getValue();
        if (selected == null) {
            return;
        }
        try {
            repositoryService.deleteRepository(selected.getId());
            PluginRepository newSelected = repositoryService.getSelectedRepository();
            repositorySelector.setItems(repositoryService.findAll());
            activeRepositoryId = null;
            repositorySelector.setValue(newSelected);
        } catch (RuntimeException exception) {
            NotificationUtils.error(exception.getMessage());
        }
    }

    private static boolean hasInstallPermission() {
        UserSession session = UserSession.getCurrent();
        return session != null && session.hasPermission(PluginPermissions.INSTALL_PLUGIN);
    }

    private static boolean requireRepositoryManagementPermission() {
        if (hasInstallPermission()) {
            return true;
        }
        NotificationUtils.info("You don't have permission to manage plugin repositories");
        return false;
    }
}

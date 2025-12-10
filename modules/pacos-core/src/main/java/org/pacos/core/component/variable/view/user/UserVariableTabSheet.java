package org.pacos.core.component.variable.view.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import org.pacos.base.component.NoContent;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.TabSheetUtils;
import org.pacos.common.view.tab.TabMonitoring;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.event.user.PasteToClipboardSelectedVariables;
import org.pacos.core.component.variable.event.user.PasteVariableEvent;
import org.pacos.core.component.variable.event.user.SaveUserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public class UserVariableTabSheet extends Div {

    private final Map<UserVariableCollectionDTO, TabMonitoring> userVariableCollectionDTOTabMap = new HashMap<>();
    private final transient UserVariableSystem system;
    private final TabSheetUtils tabSheet;
    private final NoContent noContent;
    private final Map<Tab, UserVariableForm> contentMap = new HashMap<>();
    private final transient VariableMenuBar menu;

    public UserVariableTabSheet(UserVariableSystem system) {
        this.system = system;
        this.tabSheet = new TabSheetUtils();
        this.menu = new VariableMenuBar(system);
        system.setVariableGrid(this);

        Button closeBtn = new ButtonUtils("Close all", e -> closeAll()).tabsheetBtn();
        closeBtn.getElement().setAttribute("aria-label", "Close all");
        tabSheet.setSuffixComponent(closeBtn);

        Button saveButton = new ButtonUtils("Save", e -> saveVariables()).tabsheetBtn();

        saveButton.getElement().setAttribute("aria-label", "Save");
        saveButton.setTooltipText("Ctrl + s");
        tabSheet.setPrefixComponent(saveButton);

        system.subscribe(UserVariableEvent.OPEN_COLLECTION_VARIABLE_TAB, o -> openTab((UserVariableCollectionDTO) o));
        system.subscribe(UserVariableEvent.CLOSE_ALL_TABS, o -> closeAll());
        system.subscribe(UserVariableEvent.CLOSE_TAB, o -> closeTab((UserVariableCollectionDTO) o));
        system.subscribe(UserVariableEvent.REFRESH_COLLECTION_NAME, o -> refreshTabName((UserVariableCollectionDTO) o));
        noContent = new NoContent();
        refreshContent();

        add(noContent);
        add(menu);
        add(tabSheet);

        system.subscribe(UserVariableEvent.SAVE_SHORTCUT_EVENT, e -> saveVariables());
        UISystem.getCurrent().subscribe(UserVariableEvent.REFRESH_VARIABLE, e -> refreshVariables((UserVariableDTO) e));

        system.subscribe(UserVariableEvent.COPY_SHORTCUT_EVENT, e -> onCopyVariablesEvent());
        system.subscribe(UserVariableEvent.DELETE_SHORTCUT_EVENT, e -> onElementDeletedEvent());
        system.subscribe(UserVariableEvent.PASTE_SHORTCUT_EVENT, e -> onPasteVariablesEvent());
    }

    private void refreshVariables(UserVariableDTO e) {
        contentMap.values().forEach(form -> form.refreshVariableChangesFromPlugin(e));
    }

    private void onPasteVariablesEvent() {
        getSelectedTab().ifPresent(selectedTab -> {
            if (selectedTab.isEditorClosed()) {
                PasteVariableEvent.fireEvent(system, getSelectedTab().get());
            }
        });
    }

    private void onCopyVariablesEvent() {
        getSelectedTab().ifPresent(selectedTab -> {
            if (selectedTab.isEditorClosed() && !getSelectedTab().get()
                    .getSelectedItems()
                    .isEmpty()) {
                PasteToClipboardSelectedVariables.fireEvent(getSelectedTab().get().getSelectedItems());
            }
        });
    }

    private void onElementDeletedEvent() {
        getSelectedTab().ifPresent(selectedTab -> {
            if (selectedTab.isEditorClosed() && !getSelectedTab().get()
                    .getSelectedItems()
                    .isEmpty()) {
                for (UserVariableDTO userVariableDTO : getSelectedTab().get().getSelectedItems()) {
                    getSelectedTab().get().removeParam(userVariableDTO);
                }
            }
        });
    }

    private void saveVariables() {
        this.userVariableCollectionDTOTabMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(tabSheet.getSelectedTab()))
                .findFirst().ifPresent(entry -> {
                    UserVariableForm form = (UserVariableForm) this.tabSheet.getComponent(entry.getValue());
                    SaveUserVariableEvent.fireEvent(system, entry.getKey(), form);
                });
    }

    private void refreshTabName(UserVariableCollectionDTO o) {
        if (userVariableCollectionDTOTabMap.containsKey(o)) {
            userVariableCollectionDTOTabMap.get(o).setLabel(o.getName());
        }
    }

    private void openTab(UserVariableCollectionDTO e) {
        if (userVariableCollectionDTOTabMap.get(e) == null) {
            final TabMonitoring tab = new TabMonitoring(e.getName());
            tab.add(generateCloseBtn(e));
            UserVariableForm content = new UserVariableForm(system, e, tab);
            contentMap.put(tab, content);
            userVariableCollectionDTOTabMap.put(e, tab);
            tabSheet.add(tab, content);
        }
        tabSheet.setSelectedTab(userVariableCollectionDTOTabMap.get(e));
        refreshContent();
    }

    private Button generateCloseBtn(UserVariableCollectionDTO dto) {
        return new ButtonUtils().tabCloseBtn().withClickListener(e -> closeTab(dto));
    }

    private void closeTab(UserVariableCollectionDTO dto) {
        if (userVariableCollectionDTOTabMap.containsKey(dto)) {
            Tab tab = userVariableCollectionDTOTabMap.get(dto);
            contentMap.remove(tab);
            tabSheet.remove(tab);
            userVariableCollectionDTOTabMap.remove(dto);

        }
        refreshContent();
    }

    public void closeAll() {
        userVariableCollectionDTOTabMap.values().forEach(tabSheet::remove);
        userVariableCollectionDTOTabMap.clear();
        refreshContent();
    }

    private void refreshContent() {
        tabSheet.setVisible(tabSheet.getSelectedTab() != null);
        menu.setVisible(tabSheet.getSelectedTab() != null);
        noContent.setVisible(tabSheet.getSelectedTab() == null);
    }

    Optional<UserVariableForm> getSelectedTab() {
        if (tabSheet.getSelectedTab() != null) {
            return Optional.of(contentMap.get(tabSheet.getSelectedTab()));
        }
        return Optional.empty();
    }

    TabSheetUtils getTabSheet() {
        return tabSheet;
    }
}

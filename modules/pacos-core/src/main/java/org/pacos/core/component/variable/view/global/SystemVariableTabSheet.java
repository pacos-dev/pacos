package org.pacos.core.component.variable.view.global;

import java.util.HashMap;
import java.util.Map;

import org.pacos.base.component.NoContent;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.TabSheetUtils;
import org.pacos.common.view.tab.TabMonitoring;
import org.pacos.core.component.variable.VariablePermissions;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.event.global.SaveGlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableEvent;
import org.pacos.core.component.variable.system.global.GlobalVariableSystem;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;

public class SystemVariableTabSheet extends Div {

    private final Map<SystemVariableDTO, TabMonitoring> globalVariableDTOTabMap = new HashMap<>();
    private final transient GlobalVariableSystem system;
    private final TabSheetUtils tabSheet;
    private final NoContent noContent;

    public SystemVariableTabSheet(GlobalVariableSystem system) {
        this.system = system;
        tabSheet = new TabSheetUtils();
        noContent = new NoContent();
        Button saveBtn = new ButtonUtils("Save", e -> save()).tabsheetBtn()
                .withEnabledForPermission(VariablePermissions.EDIT_GLOBAL_VARIABLE);
        saveBtn.setTooltipText("Ctrl + S");
        tabSheet.setPrefixComponent(saveBtn);

        Button plusButton = new ButtonUtils("Close all", e -> closeAll()).tabsheetBtn()
                .withEnabledForPermission(VariablePermissions.EDIT_GLOBAL_VARIABLE);

        plusButton.getElement().setAttribute("aria-label", "Add tab");
        tabSheet.setSuffixComponent(plusButton);

        system.subscribe(GlobalVariableEvent.OPEN_GLOBAL_VARIABLE_TAB, e -> openTab((SystemVariableDTO) e));
        system.subscribe(GlobalVariableEvent.REMOVED_ENTRY, e -> closeTab((SystemVariableDTO) e));
        system.subscribe(GlobalVariableEvent.REFRESH_ENTRY, (e -> globalVariableDTOTabMap.get(e).markAsNoChanges()));
        add(noContent);
        add(tabSheet);
        refreshState();
    }

    private void openTab(SystemVariableDTO e) {
        if (globalVariableDTOTabMap.get(e) == null) {
            final TabMonitoring tab = new TabMonitoring(e.getName());
            tab.add(generateCloseBtn(e));
            tabSheet.add(tab, new VariableForm(system, e, tab));
            globalVariableDTOTabMap.put(e, tab);
        }
        if (!tabSheet.getSelectedTab().equals(globalVariableDTOTabMap.get(e))) {
            tabSheet.setSelectedTab(globalVariableDTOTabMap.get(e));
        }
        refreshState();
    }

    private Button generateCloseBtn(SystemVariableDTO dto) {
        return new ButtonUtils().tabCloseBtn().withClickListener(e -> closeTab(dto));
    }

    private void closeTab(SystemVariableDTO dto) {
        if (globalVariableDTOTabMap.containsKey(dto)) {
            tabSheet.remove(globalVariableDTOTabMap.get(dto));
            globalVariableDTOTabMap.remove(dto);
        }
        refreshState();
    }

    private void closeAll() {
        globalVariableDTOTabMap.values().forEach(tabSheet::remove);
        globalVariableDTOTabMap.clear();
        refreshState();
    }

    private void refreshState() {
        noContent.setVisible(tabSheet.getSelectedTab() == null);
        tabSheet.setVisible(tabSheet.getSelectedTab() != null);
    }

    public void save() {
        if (!UserSession.getCurrent().hasPermission(VariablePermissions.EDIT_GLOBAL_VARIABLE)) {
            return;
        }

        final Tab tab = tabSheet.getSelectedTab();
        if (tab == null) {
            return;
        }
        VariableForm variableForm = (VariableForm) tabSheet.getComponent(tab);
        if (variableForm.isValid()) {
            SaveGlobalVariableEvent.fireEvent((TabMonitoring) tab, variableForm.getBean(), system);
        }
    }
}

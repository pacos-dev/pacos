package org.pacos.core.component.variable.view.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.select.Select;
import org.pacos.base.component.NoContent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.SpanUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.DialogJS;
import org.pacos.common.view.grid.decorator.BlurAndFocusDecorator;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.event.user.PasteToClipboardSelectedVariables;
import org.pacos.core.component.variable.event.user.UserVariableCollectionChangeEvent;
import org.pacos.core.component.variable.proxy.UserVariableCollectionProxy;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.view.config.VariableConfig;
import org.pacos.core.component.variable.view.config.VariablePluginHelpConfig;

public class VariablePlugin extends Dialog {
    private final PluginVariableGrid variableGrid;
    private final PluginVariableGrid globalGrid;
    private final transient UserVariableCollectionProxy service;
    private final NoContent globalGridNoContent;
    private final NoContent variableGridNoContent;
    private final transient UserVariableProxy userVariableProxy;
    private transient List<UserVariableCollectionDTO> collectionDTOList;
    private final transient UserVariableCollectionDTO globalCollection;

    public VariablePlugin(UserVariableCollectionProxy userVariableCollectionProxy,
                          UserVariableProxy userVariableProxy,
                          UserProxyService userProxyService) {
        this.service = userVariableCollectionProxy;
        this.userVariableProxy = userVariableProxy;
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        getFooter().removeAll();
        getHeader().removeAll();
        setWidth(700, Unit.PIXELS);
        setHeight(570, Unit.PIXELS);
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        SpanUtils link = buildLink();
        SpanUtils link2 = buildLink();

        this.globalGridNoContent = new NoContent("You don't have configured global variables", " Let's change that!")
                .add(link).position(55, 60).width(400);
        this.variableGridNoContent = new NoContent("You don't have configured scope variables", " Let's change that!")
                .add(link2).position(55, 60).width(400);

        this.variableGrid = new PluginVariableGrid(userVariableProxy, userVariableCollectionProxy);
        this.variableGrid.setHeight(240, Unit.PIXELS);

        this.globalGrid = new PluginVariableGrid(userVariableProxy, userVariableCollectionProxy);
        this.globalGrid.setHeight(240, Unit.PIXELS);


        this.collectionDTOList = new ArrayList<>(service.loadUserCollections(UserSession.getCurrent().getUserId()));
        this.globalCollection = fetchGlobalCollection(userVariableCollectionProxy);
        globalGrid.setVariables(userVariableProxy.loadVariables(globalCollection.getId()));

        Select<UserVariableCollectionDTO> collectionDTOSelect = new CollectionSelect();
        collectionDTOSelect.setItems(collectionDTOList);

        Integer variableCollectionId = UserSession.getCurrent().getUser().getVariableCollectionId();
        UserVariableCollectionDTO selected = collectionDTOList.stream().filter(e -> e.getId().equals(variableCollectionId)).findFirst().orElse(null);
        collectionDTOSelect.setValue(selected);
        if (selected == null) {
            variableGrid.setVariables(new ArrayList<>());
        } else {
            variableGrid.setVariables(userVariableProxy.loadVariables(selected.getId()));
        }


        collectionDTOSelect.addValueChangeListener(e -> {
            if (isOpened()) {
                UserVariableCollectionChangeEvent.fireEvent(e.getValue(), UISystem.getCurrent(), userProxyService);
                if (e.getValue() != null) {
                    this.variableGrid.setVariables(userVariableProxy.loadVariables(e.getValue().getId()));
                } else {
                    this.variableGrid.setVariables(new ArrayList<>());
                }
                refreshState();
            }
        });

        ButtonUtils manageCollectionBtn = new ButtonUtils("Manage collections", e -> {
            DesktopWindow dw = UISystem.getCurrent().getWindowManager().showWindow(VariableConfig.class);
            UISystem.getCurrent().getWindowManager().showAndMoveToFront(dw);
            close();
        }).primaryLayout().floatRight();

        ButtonUtils helpBtn = new ButtonUtils(VaadinIcon.QUESTION_CIRCLE_O.create(), e -> {
            DesktopWindow dw = UISystem.getCurrent().getWindowManager().showWindow(VariablePluginHelpConfig.class);
            UISystem.getCurrent().getWindowManager().showAndMoveToFront(dw);
            close();
        }).infoLayout().floatRight();

        HorizontalLayout content = new HorizontalLayout(new DivUtils()
                .withStyle("height", "30px")
                .withComponents(new DivUtils().withText("Active collection")
                        .withStyle("padding-top", "10px")
                        .withStyle("font-weight", "400")
                        .withStyle("text-shadow", " 0 0 #0b0b0b")
                        .withStyle("width", "120px")),
                collectionDTOSelect,
                manageCollectionBtn,
                helpBtn);
        content.getStyle().set("background-color", "var(--app-header-2)");

        content.setPadding(true);
        add(content);

        add(new Scroller(variableGrid));
        add(variableGridNoContent);
        add(new DivUtils()
                .withStyle("background-color", "var(--app-header-2)")
                .withStyle("height", "30px")
                .withComponents(new DivUtils().withText("Global scope")
                        .withStyle("padding-top", "6px")
                        .withStyle("margin", "auto")
                        .withStyle("font-weight", "400")
                        .withStyle("text-shadow", " 0 0 #0b0b0b")
                        .withStyle("width", "100px")));
        add(new Scroller(globalGrid));
        add(globalGridNoContent);

        UISystem.getCurrent().subscribe(UserVariableEvent.REFRESH_COLLECTION_LIST, e -> onRefreshCollectionListEvent(collectionDTOSelect, variableCollectionId));
        UISystem.getCurrent().subscribe(UserVariableEvent.REFRESH_VARIABLE_LIST, e -> onRefreshVariableListEvent((UserVariableCollectionDTO) e, collectionDTOSelect));

        refreshState();

        BlurAndFocusDecorator.configureFor(variableGrid);
        BlurAndFocusDecorator.configureFor(globalGrid);

        addOpenedChangeListener(this::addCopyShortcutListener);

        DialogJS.setAbsolutePosition("unset", "25px", "0", this);
    }

    private UserVariableCollectionDTO fetchGlobalCollection(UserVariableCollectionProxy userVariableCollectionProxy) {
        UserVariableCollectionDTO collectionDTO;
        Optional<UserVariableCollectionDTO> globalOpt = collectionDTOList.stream()
                .filter(UserVariableCollectionDTO::isGlobal).findFirst();

        if (globalOpt.isEmpty()) {
            collectionDTO = userVariableCollectionProxy.createGlobalCollection(UserSession.getCurrent().getUserId());
        } else {
            collectionDTO = globalOpt.get();
            collectionDTOList.remove(collectionDTO);
        }
        return collectionDTO;
    }

    private void onRefreshCollectionListEvent(Select<UserVariableCollectionDTO> collectionDTOSelect, Integer variableCollectionId) {
        refreshCollectionList(collectionDTOSelect, variableCollectionId);
        refreshState();
    }

    private void onRefreshVariableListEvent(UserVariableCollectionDTO e, Select<UserVariableCollectionDTO> collectionDTOSelect) {
        UserVariableCollectionDTO refreshedCollection = e;
        if (collectionDTOSelect.getValue() != null && refreshedCollection.equals(collectionDTOSelect.getValue())) {
            variableGrid.updateVariableList(refreshedCollection.getVariables());
        }
        if (refreshedCollection.getId().equals(globalCollection.getId())) {
            globalGrid.updateVariableList(refreshedCollection.getVariables());
        }
        refreshState();
    }

    private SpanUtils buildLink() {
        SpanUtils openVariableBtn = new SpanUtils("[Click here!]").withStyle("color", "var(--app-link)")
                .withStyle("font-style", "italic");
        openVariableBtn.addClickListener(e -> {
            UISystem.getCurrent().getWindowManager().showWindow(VariableConfig.class);
            close();
        });
        return openVariableBtn;
    }


    private void refreshState() {
        variableGrid.setVisible(!collectionDTOList.isEmpty());
        variableGridNoContent.setVisible(collectionDTOList.isEmpty());

        globalGrid.setVisible(!globalGrid.getVariables().isEmpty());
        globalGridNoContent.setVisible(globalGrid.getVariables().isEmpty());
    }

    private void refreshCollectionList(Select<UserVariableCollectionDTO> collectionDTOSelect, Integer variableCollectionId) {
        this.collectionDTOList = new ArrayList<>(service.loadUserCollections(UserSession.getCurrent().getUserId()));

        collectionDTOList.remove(globalCollection);

        collectionDTOSelect.setItems(collectionDTOList);
        if (UserSession.getCurrent().getUser().getVariableCollectionId() == null) {
            collectionDTOSelect.setValue(null);
            this.variableGrid.setVariables(null);
        } else {
            UserVariableCollectionDTO s = collectionDTOList.stream().filter(es -> es.getId().equals(variableCollectionId))
                    .findFirst().orElse(null);
            if (s != null) {
                collectionDTOSelect.setValue(s);
                this.variableGrid.setVariables(userVariableProxy.loadVariables(s.getId()));
            }
        }
    }

    @Override
    public void open() {
        super.open();
    }

    private void addCopyShortcutListener(Dialog.OpenedChangeEvent e) {
        if (e.isOpened()) {
            getElement().executeJs(
                    "document.addEventListener('keydown', function(e) {" +
                            "   if (e.ctrlKey && e.key === 'c') {" +
                            "       $0.$server.copyVariablesToClipboard();" +  // Wywołanie metody serwerowej
                            "   }" +
                            "});",
                    this
            );
        }
    }

    //Called when client uses shortcut ctrl + c on selected line in grid
    @ClientCallable
    public void copyVariablesToClipboard() {
        if (this.isOpened()) {
            getActiveGrid().ifPresent(grid ->
                    PasteToClipboardSelectedVariables.fireEvent(grid.getSelectedItems()));
                    }
    }

    private Optional<PluginVariableGrid> getActiveGrid() {
        if (globalGrid.getElement().hasAttribute("focused")) {
            return Optional.of(globalGrid);
        }
        if (variableGrid.getElement().hasAttribute("focused")) {
            return Optional.of(variableGrid);
        }
        return Optional.empty();
    }
}

package org.pacos.core.component.variable.view.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import org.pacos.base.component.Theme;
import org.pacos.base.session.UserSession;
import org.pacos.common.view.grid.GridEditableNameExtension;
import org.pacos.common.view.grid.GridFilterNameExtension;
import org.pacos.common.view.grid.decorator.BlurAndFocusDecorator;
import org.pacos.core.component.variable.dto.UserVariableCollectionDTO;
import org.pacos.core.component.variable.event.user.EditCollectionEvent;
import org.pacos.core.component.variable.event.user.RemoveCollectionEvent;
import org.pacos.core.component.variable.event.user.RenameCollectionEvent;
import org.pacos.core.component.variable.system.user.UserVariableEvent;
import org.pacos.core.component.variable.system.user.UserVariableSystem;

public class UserVariableCollectionGrid extends Grid<UserVariableCollectionDTO> {

    private final List<UserVariableCollectionDTO> items;
    private final transient UserVariableSystem system;
    private final GridEditableNameExtension<UserVariableCollectionDTO> editableName;

    public UserVariableCollectionGrid(UserVariableSystem system) {
        super(UserVariableCollectionDTO.class, false);
        system.setCollectionGrid(this);
        addThemeName("no_header");
        addThemeName(Theme.APP_STYLE.getName());
        addThemeName(Theme.NO_SELECTABLE.getName());
        addThemeName(Theme.NO_BORDER.getName());
        this.system = system;
        this.items = loadItems(UserSession.getCurrent().getUserId());

        Column<UserVariableCollectionDTO> nameCol = addComponentColumn(new UserCollectionValueProvider());
        setSizeFull();
        addItemDoubleClickListener(e -> EditCollectionEvent.fireEvent(e.getItem(), system));

        new UserCollectionContextMenu(this, system);

        this.editableName = configureEditableName(system, nameCol);
        setItems(items);
        new GridFilterNameExtension<>(this, nameCol);

        BlurAndFocusDecorator.configureFor(this);

        system.subscribe(UserVariableEvent.DELETE_SHORTCUT_EVENT, e -> onElementDeletedEvent(system));
        system.subscribe(UserVariableEvent.ADD_NEW_COLLECTION_TO_GRID, o -> addCollection((UserVariableCollectionDTO) o));
        system.subscribe(UserVariableEvent.COLLECTION_REMOVED, o -> removeCollection((UserVariableCollectionDTO) o));
        system.subscribe(UserVariableEvent.REFRESH_COLLECTION_NAME, o -> refresh((UserVariableCollectionDTO) o));


    }

    private void onElementDeletedEvent(UserVariableSystem system) {
        if (getElement().hasAttribute("focused") && !getSelectedItems().isEmpty()) {
            RemoveCollectionEvent.fireEvent(system);
        }
    }

    private List<UserVariableCollectionDTO> loadItems(int userId) {
        List<UserVariableCollectionDTO> collections = new ArrayList<>(system.getUserVariableCollectionProxy()
                .loadUserCollections(userId));

        UserVariableCollectionDTO global = collections.stream().filter(UserVariableCollectionDTO::isGlobal)
                .findFirst().orElse(UserVariableCollectionDTO.builder()
                        .name(UserVariableCollectionDTO.GLOBAL_NAME)
                        .global(true)
                        .userId(userId).variables(new ArrayList<>()).build()
                );
        collections.remove(global);
        collections.add(0, global);
        return collections;
    }

    public void editItem(UserVariableCollectionDTO e) {
        editableName.editItem(e);
    }

    private GridEditableNameExtension<UserVariableCollectionDTO> configureEditableName(UserVariableSystem system, Column<UserVariableCollectionDTO> nameCol) {
        return new GridEditableNameExtension<>(this, nameCol) {
            @Override
            public Setter<UserVariableCollectionDTO, String> setValue() {
                return UserVariableCollectionDTO::setName;
            }

            @Override
            public ValueProvider<UserVariableCollectionDTO, String> getValue() {
                return UserVariableCollectionDTO::getName;
            }

            @Override
            public void changeEvent(UserVariableCollectionDTO bean) {
                RenameCollectionEvent.fireEvent(bean, system);
            }
        };
    }


    private void removeCollection(UserVariableCollectionDTO collectionDTO) {
        items.remove(collectionDTO);
        setItems(items);
    }

    private void addCollection(UserVariableCollectionDTO collectionDTO) {
        items.add(collectionDTO);
        setItems(items);
        EditCollectionEvent.fireEvent(collectionDTO, system);
    }

    private void refresh(UserVariableCollectionDTO collectionDTO) {
        getDataCommunicator().refresh(collectionDTO);
    }


    public Optional<UserVariableCollectionDTO> getSelectedCollection() {
        if (system.getCollectionGrid().getSelectedItems().isEmpty()) {
            return Optional.empty();
        }
        UserVariableCollectionDTO selected = system.getCollectionGrid().getSelectedItems().iterator().next();
        if (selected == null) {
            return Optional.empty();
        }
        return Optional.of(selected);
    }
}



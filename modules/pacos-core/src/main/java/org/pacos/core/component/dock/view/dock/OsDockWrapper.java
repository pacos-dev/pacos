package org.pacos.core.component.dock.view.dock;

import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import org.pacos.core.component.dock.proxy.DockServiceProxy;

/**
 * Div wrapper for dock on which mouse listener is added.
 * The dock is hidden when the cursor is outside of it, and is visible when the cursor is over the wrapper dock
 */
public class OsDockWrapper extends Div implements DropTarget<OsDockElement> {

    public OsDockWrapper(DockServiceProxy dockServiceProxy) {
        setId("dockContainer");
        Div wrapper = new Div();
        wrapper.setId("dockWrapper");
        add(wrapper);
        Div desk = new Div();
        desk.setId("desk");
        wrapper.add(new OsDock(dockServiceProxy), desk);
    }
}

package org.pacos.core.component.dock.view.dock;

import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;

/**
 * Div wrapper for dock on which mouse listener is added.
 * The dock is hidden when the cursor is outside of it, and is visible when the cursor is over the wrapper dock
 */
public class OsDockWrapper extends Div implements DropTarget<OsDockElement> {

    public OsDockWrapper(OsDock osDock) {
        setId("dockContainer");
        add(osDock);
    }
}

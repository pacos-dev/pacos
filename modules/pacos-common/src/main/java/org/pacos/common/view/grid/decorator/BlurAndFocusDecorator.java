package org.pacos.common.view.grid.decorator;

import com.vaadin.flow.component.grid.Grid;

/**
 * Introduces manipulation of the focused attribute depending on the event
 */
public final class BlurAndFocusDecorator {

    private BlurAndFocusDecorator() {
    }

    public static void configureFor(Grid<?> grid) {
        grid.addFocusListener(c -> grid.getElement().setAttribute("focused", true));
        grid.addBlurListener(e -> grid.getElement().setAttribute("focused", false));
    }

}

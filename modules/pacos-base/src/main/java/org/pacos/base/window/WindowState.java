package org.pacos.base.window;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.Unit;
import org.pacos.base.window.event.OnConfirmEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the physical configuration of the window such as size, position and behavior configuration.
 * The configuration can be applied to any {@link DesktopWindow}
 */
public class WindowState {

    protected boolean draggable = true;
    protected boolean resizable = true;
    protected boolean modal = false;
    protected boolean minimizeAllowed = true;
    protected boolean cancellable = false;
    protected OnConfirmEvent confirmEvent;
    protected int width;
    protected int height;

    protected final List<Component> footerComponent = new ArrayList<>();

    /**
     * Default configuration set for modal window
     */
    public WindowState modal() {
        withResizable(false)
                .withMinimizeAllowed(false)
                .setCancellable(true)
                .withModal(true);
        return this;
    }

    /**
     * Indicate whether the window can be draggable by the user or not
     */
    public WindowState withDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    /**
     * Indicate whether the window can be resized by the user or not
     */
    public WindowState withResizable(boolean resizable) {
        this.resizable = resizable;
        return this;
    }

    /**
     * If set to true, the window is at the very front and blocks interactions with the rest of the interface as
     * long as it remains open.
     */
    public WindowState withModal(boolean modal) {
        this.modal = modal;
        return this;
    }

    /**
     * Show button in window header with ability to minimize window
     */
    public WindowState withMinimizeAllowed(boolean minimizeAllowed) {
        this.minimizeAllowed = minimizeAllowed;
        return this;
    }

    /**
     * Set window width in PIXEL
     */
    public WindowState withWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * Set window height in PIXEL
     */
    public WindowState withHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * Apply the configuration to given window
     */
    public void apply(DesktopWindow dialog) {
        dialog.setResizable(resizable);
        dialog.setModality(ModalityMode.STRICT);
        dialog.setDraggable(draggable);

        dialog.getWindowHeader().getMinimizeBtn().setVisible(minimizeAllowed);
        dialog.getWindowHeader().getExpandBtn().setVisible(resizable);

        dialog.setWidth(width, Unit.PIXELS);
        dialog.setHeight(height, Unit.PIXELS);

        dialog.getFooter().removeAll();
        if (!footerComponent.isEmpty()) {
            for (Component c : footerComponent) {
                dialog.getFooter().add(c);
            }

        }
        if (cancellable) {
            dialog.withCancelFooterBtn();
        }
        if (confirmEvent != null) {
            dialog.withConfirmationFooterBtn(confirmEvent);
        }
    }

    /**
     * Add a cancel button to the window footer that will close the window when pressed
     */
    public WindowState setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
        return this;
    }

    /**
     * Add a confirmation button to the footer of the page which, when pressed, will trigger the passed listener
     */
    public WindowState withConfirmEvent(OnConfirmEvent confirmEvent) {
        this.confirmEvent = confirmEvent;
        return this;
    }

    /**
     * Add any component to the window footer
     */
    public void addFooterComponent(Component component) {
        footerComponent.add(component);
    }
}

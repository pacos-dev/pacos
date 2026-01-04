package org.pacos.core.component.dock.view.dock;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.popover.PopoverVariant;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.dock.dto.DockConfigurationDTO;
import org.pacos.core.component.dock.proxy.DockServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Tag("li")
public class OsDockElement extends HtmlContainer implements ClickNotifier<OsDockElement>, DragSource<OsDockElement> {

    private static final Logger LOG = LoggerFactory.getLogger(OsDockElement.class);

    private final transient WindowConfig config;
    private final OsDock osDock;
    private final transient DockServiceProxy dockServiceProxy;
    private transient DockConfigurationDTO dockConfig;
    private DesktopWindow activeInstance;

    public OsDockElement(OsDock osDock, WindowConfig config, DockConfigurationDTO dockConfig,
            DockServiceProxy dockServiceProxy) {
        this.osDock = osDock;
        this.dockConfig = dockConfig;
        this.config = config;
        this.dockServiceProxy = dockServiceProxy;

        createPopover(config);

        Element link = ElementFactory.createParagraph();
        String icon = config.icon();

        Image image;
        if(icon!=null) {
            image = new Image(icon, icon);
        }else{
            image = new Image();
        }

        image.getStyle().set("user-select", "none");
        link.appendChild(image.getElement());
        getElement().appendChild(link);
        HtmlContainer active = new HtmlContainer("div");
        active.setClassName("flara");
        getElement().appendChild(active.getElement());
        addClickListener(e -> openOrHideWindowOnUserClickEvent());
        OsDockElementContextMenu.configureFor(this, config);
    }

    private void createPopover(WindowConfig config) {
        Popover namePopover = new Popover();
        namePopover.addThemeVariants(PopoverVariant.AURA_ARROW);
        namePopover.addThemeName("dock");
        namePopover.setPosition(PopoverPosition.END_TOP);
        namePopover.setOpenOnHover(true);
        namePopover.setHideDelay(10);
        namePopover.setHoverDelay(0);
        namePopover.add(new Text(config.title()));
        add(namePopover);
        namePopover.setTarget(this);
    }

    static UISystem getUiSystem() {
        return UserSession.getCurrent().getUISystem();
    }

    void setActiveInstance(DesktopWindow activeInstance) {
        this.activeInstance = activeInstance;
    }

    DockConfigurationDTO getUserConfig() {
        return dockConfig;
    }

    void storeActivator(boolean value) {
        if (value) {
            this.dockConfig = dockServiceProxy
                    .addActivator(config.activatorClass().getSimpleName(), UserSession.getCurrent().getUserId());
        } else {
            dockServiceProxy
                    .removeActivator(config.activatorClass().getSimpleName(), UserSession.getCurrent().getUserId());
            this.dockConfig = null;
            if (activeInstance == null) {
                osDock.removeDockElement(config, false);
            }
        }
    }

    boolean isActive() {
        return getUiSystem().getWindowManager().isActive(config);
    }

    void close() {
        List<DesktopWindow> activeInstances = getUiSystem().getWindowManager().getInitializedWindows(config);
        if (activeInstances != null) {
            activeInstances = new ArrayList<>(activeInstances);
            for (DesktopWindow ins : activeInstances) {
                try {
                    getUiSystem().notify(ModuleEvent.MODULE_SHUTDOWN, ins);
                } catch (Exception e) {
                    LOG.error("Error");
                }
            }
        }
    }

    boolean isStored() {
        return dockConfig != null;
    }

    WindowConfig getModuleConfig() {
        return config;
    }

    void openOrHideWindowOnUserClickEvent() {
        if (config.isApplication() && !config.isAllowMultipleInstance()) {
            activeInstance = getUiSystem().getWindowManager().showWindow(config.getClass());
        } else if (activeInstance != null) {
            getUiSystem().getWindowManager().showOrHideAlreadyCreatedWindow(activeInstance, false);
        }
    }
}

package org.pacos.base.window;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.exception.PacosException;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.base.window.config.impl.ModalWindowConfig;
import org.pacos.base.window.config.impl.WarningWindowConfig;
import org.pacos.base.window.event.OnConfirmEvent;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.base.window.shortcut.Shortcut;
import org.pacos.base.window.shortcut.ShortcutType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(registerAtStartup = false)
public abstract class DesktopWindow extends Dialog {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DesktopWindow.class);

    private final transient WindowConfig moduleConfig;
    private final transient ExpandFunction expandInfo;

    protected final transient UISystem uiSystem;
    private final WindowHeader header;

    private final List<DesktopWindow> childWindow = new ArrayList<>();
    private final VerticalLayout content;
    private DesktopWindow parentWindow;

    private UI ui;
    protected UserSession session;

    protected DesktopWindow(WindowConfig moduleConfig) {
        this(moduleConfig, configureLayout());
    }

    private static VerticalLayout configureLayout() {
        VerticalLayout layout = new VerticalLayout();
        return VerticalLayoutUtils.configure(layout);
    }

    protected DesktopWindow(WindowConfig moduleConfig, VerticalLayout content) {
        super();
        if (content == null) {
            throw new PacosException("Window content can't be null");
        }
        this.content = content;
        this.uiSystem = UserSession.getCurrent().getUISystem();
        this.expandInfo = new ExpandFunction(false, "1000px", "500px", this);
        this.expandInfo.modify();
        this.moduleConfig = moduleConfig;
        this.session = UserSession.getCurrent();
        this.addClassName("app-window");
        this.addThemeName("app-modal");
        this.addThemeName("app-dialog");
        setId("dlg-" + UUID.randomUUID());
        setDraggable(true);
        setResizable(true);
        setModal(false);
        setCloseOnOutsideClick(false);
        setCloseOnEsc(false);
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        super.add(content);

        this.header = new WindowHeader(moduleConfig.title(), this, uiSystem);
        getHeader().removeAll();
        getHeader().add(header);

        String title = moduleConfig.title();
        DialogJS.enableResizingOnHeaderDblClick(this);
        DialogJS.monitorWindowOnFront(this);

        addDetachListener(this::replaceUi);
        ui = UI.getCurrent();
        LOG.debug("Window initialized: {} by {}", title, this.session.getUser());
    }

    private void replaceUi(DetachEvent detachEvent) {
        this.ui = UI.getCurrent();
    }

    /**
     * Add components to window content
     */
    @Override
    public void add(Component... components) {
        Arrays.stream(components).forEach(content::add);
    }

    public WindowConfig getConfig() {
        return moduleConfig;
    }

    public void setPosition(String left, String top) {
        DialogJS.setPositionWithTimeout(left, top, this);
    }

    public WindowHeader getWindowHeader() {
        return header;
    }

    public void moveToFront() {
        DialogJS.moveToFront(this);
    }

    /**
     * Called by client when dbl click event on window header detected
     */
    @ClientCallable
    public void onHeaderDoubleClick() {
        getWindowHeader().onHeaderDblClick();
    }

    /**
     * Called by client when window is on top
     */
    @ClientCallable
    public void markWindowOnTop() {
        uiSystem.notify(ModuleEvent.ACTIVE_WINDOW,this);
    }
    /**
     * Add new {@link ModalWindow} as a child to this window.
     * Window will be initialized by {@link WindowManager} based on passed {@link WindowConfig}.
     * All child modules will be close and open automatically with the parent window while minimize or close
     * use addChildWindow instead which can return created window
     */
    public DesktopWindow addWindow(ModalWindowConfig config) {
        final DesktopWindow e = uiSystem.getWindowManager().showModalWindow(config);
        childWindow.add(e);
        e.parentWindow = this;
        return e;
    }

    public void addWarningModal(WarningWindowConfig config) {
        final DesktopWindow e = uiSystem.getWindowManager().showWarningWindow(config);
        childWindow.add(e);
        e.parentWindow = this;
    }

    /**
     * Window will be minimized and stored in the background for future use by user
     */
    public void minimize() {
        super.close();
    }

    /**
     * - Close window and remove from parent element on UI
     * - Close all child windows
     * - Unregister all shortcuts listeners
     * - Send notify ModuleEvent.MODULE_SHUTDOWN for all child window
     */
    @Override
    public void close() {
        super.close();
        getElement().removeFromTree();

        List<DesktopWindow> clonedChildList = new ArrayList<>(childWindow);
        clonedChildList.forEach(ch -> uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, ch));

        childWindow.clear();
        if (parentWindow != null) {
            parentWindow.childWindow.remove(this);
        }
        uiSystem.getShortcutManager().unregisterAll(this);
    }

    /**
     * Once triggered, window will be closed by window manager.
     * Active icon will be removed from menu bar
     */
    public void shutDown() {
        uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, this);
    }

    public void restorePosition() {
        if (expandInfo != null && expandInfo.isExpanded()) {
            DialogJS.setPositionWithTimeout("0px", "0px", this, 50);
        }
    }

    /**
     * The window will be closed when the user presses [Esc]
     */
    public void allowCloseOnEsc() {
        setCloseOnEsc(true);
        addDialogCloseActionListener(e -> uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, this));
    }

    /**
     * The window will be closed when the user click outside the window
     */
    public void allowCloseOnOutsideClick() {
        setCloseOnOutsideClick(true);
        addDialogCloseActionListener(e -> uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, this));
    }

    public VerticalLayout getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "DesktopWindow{\"" + moduleConfig.title() + "\"}";
    }

    /**
     * register event listener for shortcut typed by the user on UI
     *
     * @param shortcutType          already defined shortcut
     * @param shortcutEventListener listener that will be triggered after event detected
     */
    protected ShortcutRegistration registerShortcut(ShortcutType shortcutType,
            ShortcutEventListener shortcutEventListener) {
        return registerShortcut(shortcutType.getShortcut(), shortcutEventListener);
    }

    /**
     * register event listener for shortcut typed by the user on UI
     *
     * @param shortcut              any shortcut
     * @param shortcutEventListener listener that will be triggered after event detected
     */
    protected ShortcutRegistration registerShortcut(Shortcut shortcut, ShortcutEventListener shortcutEventListener) {
        return uiSystem.getShortcutManager().registerShortcut(this, shortcut, shortcutEventListener);
    }

    /**
     * Set window size in PIXEL
     */
    protected void setSize(int width, int height) {
        setWidth(width, Unit.PIXELS);
        setHeight(height, Unit.PIXELS);
    }

    /**
     * Helper methods to add a Cancel button to the window footer
     */
    void withCancelFooterBtn() {
        Button cancelBtn = new Button("Cancel", e -> uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, this));
        cancelBtn.addThemeName("info_l");
        getFooter().add(cancelBtn);
    }

    /**
     * Helper methods to add a Confirmation button to the window footer
     */
    void withConfirmationFooterBtn(OnConfirmEvent event) {
        Button buttonUtils = new Button("Ok", e -> {
            if (event.confirm()) {
                uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, this);
            }
        });
        buttonUtils.addThemeName("primary_l");
        buttonUtils.addClickShortcut(Key.ENTER);
        buttonUtils.addClickShortcut(Key.NUMPAD_ENTER);
        getFooter().add(buttonUtils);
    }

    public UI getUi() {
        return ui;
    }

    ExpandFunction getExpandInfo() {
        return expandInfo;
    }

    DesktopWindow getParentWindow() {
        return parentWindow;
    }

    List<DesktopWindow> getChildWindow() {
        return childWindow;
    }

}
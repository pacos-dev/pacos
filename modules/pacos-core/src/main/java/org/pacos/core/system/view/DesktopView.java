package org.pacos.core.system.view;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.*;
import org.pacos.base.component.editor.NativeCodeMirror;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.manager.ApplicationManager;
import org.pacos.base.window.manager.ShortcutManager;
import org.pacos.base.window.manager.WindowManager;
import org.pacos.core.component.dock.view.dock.OsDock;
import org.pacos.core.component.dock.view.dock.OsDockWrapper;
import org.pacos.core.component.menu.MenuSystem;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.settings.view.background.PredefinedBackground;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.variable.view.config.VariableManagerImpl;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.manager.*;
import org.pacos.core.system.proxy.AppProxy;
import org.pacos.core.system.theme.ThemeManager;
import org.pacos.core.system.theme.UITheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.addons.variablefield.field.VariableTextField;
import org.vaadin.addons.variablefield.modal.VariableModal;

@PageTitle("Pac OS")
@Route(value = "desktop")
@PreserveOnRefresh
@Uses(Grid.class)
@Uses(TreeGrid.class)
@Uses(TabSheet.class)
@Uses(SplitLayout.class)
@Uses(MenuBar.class)
@Uses(TextField.class)
@Uses(Button.class)
@Uses(Checkbox.class)
@Uses(TextArea.class)
@Uses(NumberField.class)
@Uses(Select.class)
@Uses(VariableTextField.class)
@Uses(VariableModal.class)
@Uses(HorizontalLayout.class)
@Uses(IntegerField.class)
@Uses(ProgressBar.class)
@Uses(Scroller.class)
@Uses(ComboBox.class)
@Uses(NativeCodeMirror.class)
@Uses(RadioButtonGroup.class)
public class DesktopView extends Div implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(DesktopView.class);
    private final transient UserProxyService proxyService;
    private final transient RegistryProxy registry;

    @Autowired
    public DesktopView(AppProxy appProxy) {
        this.registry = appProxy.getRegistryProxy();
        this.proxyService = appProxy.getUserProxyService();

        if (unprivilegedAccess(proxyService)) {
            return;
        }
        ThemeManager.setTheme(UITheme.LIGHT);
        showWelcomeNotification();
        setBackground(null);
        VariableManagerImpl variableManager = new VariableManagerImpl(PluginResource.getAllVariableProvider());
        DownloadManagerImpl downloadManager = new DownloadManagerImpl();
        WindowManager windowManager = new WindowManagerImpl();
        ShortcutManager shortcutManager = new ShortcutManagerImpl();
        ClipboardManagerImpl clipboardManager = new ClipboardManagerImpl();
        ApplicationManager applicationManager = new ApplicationManagerImpl();
        UISystem uiSystem = new UISystem(downloadManager, variableManager, windowManager,
                applicationManager, shortcutManager, clipboardManager);
        UserSession.getCurrent().setUiSystem(uiSystem);

        addAttachListener(e -> onAttachedEvent(variableManager));
        addDetachListener(this::onDetachEvent);

        add(downloadManager);
        add(clipboardManager);
        add(new MenuSystem(appProxy));
        add(new OsDockWrapper(new OsDock(appProxy.getDockServiceProxy())));
        add(new BottomGreenLine());
        uiSystem.subscribe(ModuleEvent.ACTIVE_WINDOW, dw -> windowManager.markWindowOnTop((DesktopWindow) dw));
        uiSystem.subscribe(ModuleEvent.MODULE_SHUTDOWN, e -> windowManager.close((DesktopWindow) e));
        uiSystem.subscribe(ModuleEvent.BACKGROUND_CHANGED, e -> setBackground((String)e));
    }

    private void setBackground(String location) {
        if(location==null){
            location = registry.readRegistryOrDefault(RegistryName.BACKGROUND, PredefinedBackground.NINE.getSrc());
        }
        Background.configure(this,location);
    }

    /**
     * Import all resources when view is loaded. On first enter and on refresh
     */
    void onAttachedEvent(VariableManagerImpl variableManager) {
        importEagerResources();
        add(variableManager);
        PacosJS.initializeScripts();
    }

    private static void showWelcomeNotification() {
        if (UserSession.getCurrent().getUser().isGuestSession()) {
            Notification.show("You have been logged in as a guest. " +
                    "Your session and personal configuration will not be saved and you have limited access " +
                    "to some system services.", 10000, Notification.Position.MIDDLE);
        } else {
            NotificationUtils.success("Welcome " + UserSession.getCurrent().getUserName(),
                    Notification.Position.MIDDLE);
        }
    }

    /**
     * In case when page on webBrowser is refreshed, new UI is created and UISystem assigned to detached UI must be
     * assigned to newly createdUI
     */
    void onDetachEvent(DetachEvent e) {
        if (UserSession.getCurrent() != null && UI.getCurrent() != null) {
            UserSession.getCurrent().restoreUISystem(e.getUI());
            //rebind all shortcuts
            UISystem.getCurrent().getShortcutManager().refreshShortcuts();
        }
    }

    private static void importEagerResources() {
        UI.getCurrent().getPage().addStyleSheet("frontend/css/index.css");
        UI.getCurrent().getPage().addJavaScript("frontend/js/system.js");
        UI.getCurrent().getPage().addJavaScript("frontend/js/clock/clock.js");
        UI.getCurrent().getPage().addJavaScript("frontend/js/drag/dragscript.js");
    }

    static boolean unprivilegedAccess(UserProxyService proxyService) {
        if (!UserSessionService.isLogIn(proxyService) || UserSession.getCurrent() == null
                || UserSession.getCurrent().getUser() == null) {
            UserSessionService.destroyUserSession();
            LOG.debug("Unprivileged access. Redirecting to login page by BeforeEnterListener");
            UI.getCurrent().push();
            return true;
        }
        return false;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (!UserSessionService.isLogIn(proxyService)) {
            LOG.debug("Redirect to index view");
            beforeEnterEvent.forwardTo(IndexView.class);
        }
    }

}

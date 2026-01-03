package org.pacos.core.system.window;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import org.pacos.base.component.Theme;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.ImageUtils;
import org.pacos.base.utils.component.ParagraphUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.system.event.RestartSystemEvent;
import org.pacos.core.system.window.config.LogoutWindowConfig;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

@Scope("prototype")
public class LogoutWindow extends DesktopWindow {

    public LogoutWindow(LogoutWindowConfig moduleConfig, RegistryProxy registryProxy) {
        super(moduleConfig);
        allowCloseOnEsc();
        addThemeName(Theme.BLACK_WINDOW.getName());
        this.removeThemeName("app-dialog");
        getHeader().removeAll();
        setDraggable(false);
        setResizable(false);
        setModality(ModalityMode.STRICT);
        setSize(400, 250);
        setCloseOnOutsideClick(false);

        Button cancel = new ButtonUtils("Cancel")
                .infoLayout()
                .withVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL)
                .withClickListener(e -> onCancel());

        Button logOut = new ButtonUtils("Log out")
                .errorLayout()
                .withVariants(ButtonVariant.LUMO_PRIMARY,
                        ButtonVariant.LUMO_SMALL)
                .withClickListener(e -> {
                    onCancel();
                    UserSessionService.logOut();
                });

        if (!UserSession.getCurrent().getUser().isGuestSession() && registryProxy.isRestartRequired()) {
            Button restart = new ButtonUtils("Restart server")
                    .errorLayout()
                    .withVariants(ButtonVariant.LUMO_WARNING, ButtonVariant.LUMO_SMALL)
                    .withClickListener(e -> RestartSystemEvent.fireEvent());
            restart.setTooltipText("Restart server is required to apply configuration changes. " +
                    "Click this button if you want to restart the server now.");
            getFooter().add(restart);
        }
        getFooter().add(cancel, logOut);


        List<WindowConfig> runningWindows = new ArrayList<>();
        uiSystem.getWindowManager().getAllInitializedWindows().forEach(w -> runningWindows.add(w.getConfig()));
        DivUtils windowBlock = new DivUtils();
        if (!runningWindows.isEmpty()) {
            windowBlock.add(new Paragraph("The following apps are still running:"));
            for (WindowConfig config : runningWindows) {
                final Image icon = getImage(config.icon());
                windowBlock.add(icon);
            }
        }
        add(new DivUtils().withStyle("padding", "20px").withComponents(
                new DivUtils().withStyle("display", "inline")
                        .withStyle("float", "left")
                        .withStyle("padding-top", "10px")
                        .withStyle("padding-right", "10px")
                        .withComponents(getImage(PacosIcon.POWER_OFF.getUrl())),
                new DivUtils().withStyle("display", "inline")
                        .withStyle("float", "left").withComponents(
                                new Paragraph("Are you sure you want to Log Out?"),
                                new ParagraphUtils("This will close any open apps and destroy your session")
                                        .withStyle("color", "rgb(149 149 149)")
                                        .withStyle("margin-top", "-7px"),
                                windowBlock
                        )
        ));
    }

    private static Image getImage(String iconPath) {
        final Image icon = new ImageUtils(iconPath, "icon");
        icon.setWidth(45, Unit.PIXELS);
        icon.setHeight(45, Unit.PIXELS);
        icon.getStyle().set("padding-right", "10px");
        return icon;
    }

    public void onCancel() {
        uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, this);
    }

}
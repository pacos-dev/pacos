package org.pacos.core.system.window;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import org.pacos.base.component.Spinner;
import org.pacos.base.component.Theme;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.ImageUtils;
import org.pacos.base.utils.component.ParagraphUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.system.event.RestartSystemEvent;
import org.pacos.core.system.window.config.RestartWindowConfig;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

@Scope("prototype")
public class RestartWindow extends DesktopWindow {

    public RestartWindow(RestartWindowConfig moduleConfig) {
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

        Button restart = new ButtonUtils("Restart")
                .errorLayout()
                .withVariants(ButtonVariant.LUMO_PRIMARY,
                        ButtonVariant.LUMO_SMALL)
                .withClickListener(e -> onRestartBtnClick());

        getFooter().add(cancel, restart);

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
        add(generateDescriptionBox("Are you sure you want to restart?", "This will close all sessions and open apps", windowBlock));
    }

    private void onRestartBtnClick() {
        UI.getCurrent().access(() -> {
            getContent().removeAll();
            Spinner spinner = new Spinner();
            getContent().add(generateDescriptionBox("System is restarting now", "Please wait a while, this page will be reloaded automatically", spinner));
            getFooter().removeAll();
        });
        UI.getCurrent().push();

        if (!RestartSystemEvent.fireEvent()) {
            onCancel();
        }
    }

    private DivUtils generateDescriptionBox(String p1, String p2, Component block) {
        return new DivUtils().withStyle("padding", "20px").withComponents(
                new DivUtils().withStyle("display", "inline")
                        .withStyle("float", "left")
                        .withStyle("padding-top", "10px")
                        .withStyle("padding-right", "10px")
                        .withComponents(getImage(PacosIcon.POWER_OFF.getUrl())),
                new DivUtils().withStyle("display", "inline")
                        .withStyle("float", "left").withComponents(
                                new Paragraph(p1),
                                new ParagraphUtils(p2)
                                        .withStyle("color", "rgb(149 149 149)")
                                        .withStyle("margin-top", "-7px"),
                                block
                        )
        );
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
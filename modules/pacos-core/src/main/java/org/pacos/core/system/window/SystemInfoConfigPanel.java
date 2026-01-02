package org.pacos.core.system.window;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.TimeToStringUtils;
import org.pacos.base.utils.component.*;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.system.window.config.ReleaseNoteConfig;
import org.pacos.core.system.window.config.SystemInfoConfig;
import org.springframework.context.annotation.Scope;

import java.time.LocalDateTime;

@Scope("prototype")
public class SystemInfoConfigPanel extends DesktopWindow {

    private static final String START_TIME = TimeToStringUtils.format(LocalDateTime.now());

    public SystemInfoConfigPanel(SystemInfoConfig moduleConfig, RegistryProxy registryProxy) {
        super(moduleConfig);
        this.removeThemeName("app-dialog");
        getHeader().removeAll();
        setDraggable(false);
        setResizable(false);
        setModality(ModalityMode.STRICT);
        allowCloseOnEsc();
        allowCloseOnOutsideClick();
        setSize(400, 300);
        getWindowHeader().getRight().removeAll();
        setResizable(false);
        Image icon = new ImageUtils("img/logo.png");
        icon.setClassName("logo-img");


        String availableVersion = registryProxy.isSystemToUpdate() ?
                registryProxy.readRegistryOrDefault(RegistryName.AVAILABLE_SYSTEM_VERSION,
                        "---") : "no update";
        String availablePlugin =
                registryProxy.readRegistry(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE, Integer.class, 0) > 0 ?
                        registryProxy.readRegistryOrDefault(RegistryName.AVAILABLE_PLUGINS_COUNT_TO_UPDATE,
                                "---") : "no update";

        ListContent infoContent = new ListContent("col");
        infoContent.add(icon);
        infoContent.addRow("NAME", "Pac OS", true);
        infoContent.addRow("VERSION", registryProxy.readRegistry(RegistryName.SYSTEM_VERSION).orElse("1.0"), true);
        infoContent.addRow("STARTED AT", START_TIME, false);
        infoContent.addRow("LAST UPDATED", registryProxy.readTime(RegistryName.LAST_UPDATE_TIME, "---"), false);
        infoContent.addRow("SYSTEM TO UPDATE", availableVersion, false)
                .withStyle("font-weight", registryProxy.isSystemToUpdate() ? "600" : "300")
                .withClassName(registryProxy.isSystemToUpdate() ? "red_info" : "no_info");
        infoContent.addRow("PLUGINS TO UPDATE", availablePlugin, false)
                .withStyle("font-weight", registryProxy.isPluginToUpdate() ? "600" : "300")
                .withClassName(registryProxy.isPluginToUpdate() ? "red_info" : "no_info");
        infoContent.withComponents(new Hr());
        infoContent.addRow("RELEASE NOTE",
                new SpanUtils().withComponents(new ButtonUtils("release_note.txt").infoLayout()
                        .withClickListener(e -> openReleaseNotWindow(uiSystem))));

        add(new DivUtils().withClassName("release")
                .withComponents(infoContent));
    }

    void openReleaseNotWindow(UISystem uiSystem) {
        uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, this);
        uiSystem.getWindowManager().showWindow(ReleaseNoteConfig.class);
    }

}

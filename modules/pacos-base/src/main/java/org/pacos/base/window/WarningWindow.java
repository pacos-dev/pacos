package org.pacos.base.window;

import com.vaadin.flow.component.button.ButtonVariant;
import org.pacos.base.component.Theme;
import org.pacos.base.component.icon.PacosIcon;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.HorizontalLayoutUtils;
import org.pacos.base.utils.component.ImageUtils;
import org.pacos.base.utils.component.ParagraphUtils;
import org.pacos.base.window.config.impl.WarningWindowConfig;

public class WarningWindow extends DesktopWindow {

    public WarningWindow(WarningWindowConfig config) {
        super(config);
        this.removeThemeName("app-dialog");
        getHeader().removeAll();
        allowCloseOnEsc();
        addThemeName(Theme.BLACK_WINDOW.getName());
        config.getWindowState().apply(this);

        final String marginProperty = "margin";
        final String paddingProperty = "padding";

        add(HorizontalLayoutUtils.defaults(
                new ImageUtils(PacosIcon.ALERT.getUrl(), "alert").withWidth(60)
                        .withHeight(60).withStyle(marginProperty, "auto")
                        .withStyle(paddingProperty, "30px"),
                new DivUtils().withStyle(marginProperty, "auto")
                        .withStyle("padding-right", "20px")
                        .withComponents(
                                new ParagraphUtils(config.getTitle())
                                        .withStyle("font-weight", "bold"),
                                new ParagraphUtils(config.getMessage())

                        )));
        getFooter().add(new ButtonUtils("Cancel")
                .withStyle(marginProperty, "0 20px 20px 0")
                .withStyle(paddingProperty, "0 40px")
                .infoLayout()
                .withVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL)
                .withClickListener(e -> uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, this)));
        addThemeName(Theme.BLACK_WINDOW.getName());
    }

}

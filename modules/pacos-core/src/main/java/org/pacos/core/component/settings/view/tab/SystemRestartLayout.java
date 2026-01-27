package org.pacos.core.component.settings.view.tab;

import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.security.SystemPermissions;
import org.pacos.core.system.event.RestartSystemEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;

@Component
@Scope("prototype")
public class SystemRestartLayout extends SettingPageLayout {

    private static final String RESTART_SYSTEM = "Restart system";
    private static final String INFO = "Manually restart the system";

    public SystemRestartLayout() {
        ButtonUtils restartApplication = new ButtonUtils(RESTART_SYSTEM)
                .withVariants(ButtonVariant.LUMO_PRIMARY)
                .withVisibleForPermission(SystemPermissions.SYSTEM_RESTART)
                .floatRight()
                .withClickListener(e -> restartSystem());

        add(new InfoBox(INFO));
        add(restartApplication);

    }

    private void restartSystem() {
        final ConfirmationWindowConfig config =
                new ConfirmationWindowConfig(() -> {
                    RestartSystemEvent.fireEvent();
                    return true;
                });
        config.setWarning(true);
        config.getWindowState().modal();
        config.getWindowState().withConfirmationBtnLabel("Yes, restart now");
        config.getWindowState().withWarningMode(true);
        config.setContent(VerticalLayoutUtils.defaults(
                new Paragraph("Are you sure?"),
                new Paragraph("PacOS will be restarted immediately.")
        ));
        UISystem.getCurrent().getWindowManager().showModalWindow(config);
    }

    public static String getSearchIndex() {
        return INFO + RESTART_SYSTEM;
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //not implemented
    }
}

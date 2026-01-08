package org.pacos.core.component.settings.view.tab;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.*;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.settings.view.background.PredefinedBackground;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SystemBackgroundLayout extends SettingPageLayout {

    private static final String BING_URL = "https://bing.biturl.top/?resolution=1920x1080&format=image&index=0";
    private static final String INFO = "Choose one of the preinstalled wallpapers";
    private static final String INFO2 = "Provide a URL to an existing image";
    private static final String INFO3 = "Enable a daily-changing random wallpaper powered by Bing.";

    private final transient RegistryProxy registryProxy;

    public SystemBackgroundLayout(RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
        add(new InfoBox(INFO));

        Div grid = DivUtils.ofClass("grid");

        for (PredefinedBackground bck : PredefinedBackground.values()) {
            Span div = new Span();
            Image image = new ImageUtils(bck.getSrc(), bck.getResolution());
            image.setWidth(180, Unit.PIXELS);
            image.setHeight(120, Unit.PIXELS);
            image.addClickListener(e -> updateImage(bck.getSrc()));
            image.setClassName("wall");
            image.setTitle(bck.getResolution());
            image.add();

            div.add(image);
            div.add(new Div(bck.getResolution()));
            grid.add(div);
        }
        add(grid);

        add(new Hr());
        add(new InfoBox(INFO3));
        Button dailyWallpaper = new Button("Enable daily random wallpaper");
        dailyWallpaper.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dailyWallpaper.addClickListener(e -> updateImage(BING_URL));
        add(dailyWallpaper);

        add(new Hr());
        add(new InfoBox(INFO2));
        TextFieldUtils manualUrl = new TextFieldUtils();
        manualUrl.setPlaceholder("https://example.com/image.jpg");
        manualUrl.setWidthFull();
        Button btn = new Button("Load image from URL");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout manual = HorizontalLayoutUtils.defaults(manualUrl, btn);
        add(manual);

        btn.addClickListener(e -> updateImage(manualUrl.getValue()));
    }

    private void updateImage(String src) {
        UISystem.getCurrent().notify(ModuleEvent.BACKGROUND_CHANGED, src);
        registryProxy.saveRegistry(RegistryName.BACKGROUND, src);
        if (src.equals(BING_URL)) {
            NotificationUtils.success("Random daily wallpaper enabled");
        } else {
            NotificationUtils.success("Wallpaper updated");
        }
    }

    public static String getSearchIndex() {
        return INFO + INFO2 + INFO3;
    }


    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //shortcut listener not implemented
    }
}
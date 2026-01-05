package org.pacos.core.component.menu;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.StreamResource;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.utils.component.ImageUtils;
import org.pacos.base.utils.component.TextFieldUtils;
import org.pacos.base.window.DesktopWindow;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.plugin.manager.PluginState;

import java.nio.file.Path;
import java.util.Set;

public class ApplicationsModal extends Dialog {


    private transient Set<WindowConfig> applications;
    private final Div board;
    private final ApplicationsModal dialog;
    final TextFieldUtils searchField;
    final UserSession userSession;

    public ApplicationsModal() {
        this.userSession = UserSession.getCurrent();
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        getFooter().removeAll();
        getHeader().removeAll();
        setWidth(432, Unit.PIXELS);
        setHeight(335, Unit.PIXELS);
        this.dialog = this;

        this.searchField = TextFieldUtils.configureSearchField("Find application");
        searchField.removeThemeName("no-border");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.focus();
        searchField.addValueChangeListener(e -> displayApplicationList());
        searchField.addKeyDownListener(this::onKeyDownEvent);

        add(searchField);

        this.board = new Div();
        board.addClassName("board");
        add(board);
        setTop("0px");
        setLeft("0px");
        UISystem.getCurrent().subscribeOnAttached(this, ModuleEvent.PLUGIN_INSTALL_STATE_CHANGED, e -> asyncReloadApplications());
    }

    void onKeyDownEvent(KeyDownEvent e) {
        if (e.getKey().equals(Key.ENTER)) {
            runFirstOnList(searchField.getValue());
        }
    }

    private void runFirstOnList(String filters) {
        applications.stream().filter(e -> e.title().toLowerCase().contains(filters.toLowerCase()))
                .findFirst().ifPresent(e -> onClickListener(e, dialog));
    }

    private void displayApplicationList() {
        board.removeAll();
        applications.stream().filter(e -> e.title().toLowerCase().contains(searchField.getValue().toLowerCase()))
                .forEach(e -> board.add(buildIconElement(e, dialog)));
        PluginState.isInstallationInProgress().ifPresent(plugin -> {
            Div element = new Div();
            element.addClassName("app_icon");
            element.addClassName("blink-image");
            Path icon = plugin.getIconAbsolutePath();

            final Image image = ImageUtils.fromLocation(icon, plugin.getArtifactName());
            image.addClassName("appicon");
            element.add(image);
            final Span title = new Span(plugin.getName());
            title.addClassNames("title");
            element.add(title);
            board.add(element);

        });
    }

    private static Div buildIconElement(WindowConfig config, Dialog appDialog) {
        Div element = new Div();
        element.addClassName("app_icon");
        StreamResource resource = new StreamResource("image.png", () ->
                config.getClass().getResourceAsStream("/META-INF/resources/" + config.icon()));

        Image image;
        try {
            image = new Image(resource, config.icon());
        } catch (Exception e) {
            image = new Image();
        }
        image.addClassName("appicon");
        element.add(image);
        final Span title = new Span(config.title());
        title.addClassNames("title");
        element.add(title);
        element.addClickListener(e -> onClickListener(config, appDialog));
        return element;
    }

    private static void onClickListener(WindowConfig config, Dialog appDialog) {
        appDialog.close();
        DesktopWindow dw = UISystem.getCurrent().getWindowManager().showWindow(config.getClass());
        dw.open();
        dw.moveToFront();
    }

    @Override
    public void open() {
        this.applications = PluginResource.getAppWindowConfigForUser(UserSession.getCurrent());
        searchField.setValue("");
        displayApplicationList();
        searchField.blur();
        searchField.focus();
        super.open();
    }

    private void asyncReloadApplications() {
        this.getUI().ifPresent(ui -> ui.access(() -> {
            this.applications = PluginResource.getAppWindowConfigForUser(userSession);
                    displayApplicationList();
                    ui.push();
                }
        ));
    }


}

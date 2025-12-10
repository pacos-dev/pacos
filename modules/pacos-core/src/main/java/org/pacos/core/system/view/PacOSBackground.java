package org.pacos.core.system.view;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.SpanUtils;

public class PacOSBackground extends Div {

    private final DivUtils window;

    public PacOSBackground() {
        setClassName("welcome-background");

        final DivUtils center = DivUtils.ofClass("center");
        final DivUtils centerContent = DivUtils.ofClass("login-content");
        this.window = DivUtils.ofClass("window");

        add(center);
        center.add(centerContent);
        Image image = new Image("/img/logo.png", "logo");
        image.setWidth(300, Unit.PIXELS);
        image.getStyle().set("margin-bottom", "10px");
        image.getStyle().set("margin-left", "80px");
        centerContent.add(image);

        centerContent.add(window);
        centerContent.add(DivUtils.ofClass("footer")
                .withComponents(new SpanUtils("Powered by"))
                .withComponents(new Anchor("https://pacos.dev","pacos.dev"))
                .withComponents(SpanUtils.ofClass("point"))
                .withComponents(new SpanUtils("2025"))
        );


    }

    public DivUtils getWindow() {
        return window;
    }
}

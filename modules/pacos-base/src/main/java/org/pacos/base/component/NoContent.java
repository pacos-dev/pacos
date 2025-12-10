package org.pacos.base.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.ImageUtils;

public class NoContent extends Div {

    public NoContent() {
        this("There is currently", "no content to display here");
    }

    public NoContent(String line1, String line2) {
        addClassName("no-content");
        addClassName("tetiary");
        addClassName("no-select");
        add(new DivUtils().withComponents(
                        new ImageUtils("/img/icon/pencil.png"))
                .withStyle("width", "200px")
                .withStyle("margin", "auto")
                .withStyle("margin-bottom", "10px"));

        add(new Span(line1));
        add(new Span(line2));
    }

    public NoContent position(int top, int bottom) {
        getStyle().set("margin-top", top + "px");
        getStyle().set("margin-bottom", bottom + "px");
        return this;
    }

    public NoContent add(Component component) {
        super.add(component);
        return this;
    }

    public NoContent width(int i) {
        setWidth(i, Unit.PIXELS);
        return this;
    }
}


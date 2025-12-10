package org.pacos.base.utils.component;

import java.util.Arrays;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import org.pacos.base.component.Theme;

public class SplitterUtils extends SplitLayout {

    public SplitterUtils(Component primaryComponent, Component secondaryComponent) {
        super(primaryComponent, secondaryComponent);
        this.setSizeFull();
    }

    public SplitterUtils(Component primaryComponent, Component secondaryComponent, double position) {
        super(primaryComponent, secondaryComponent);
        setSplitterPosition(position);
        setOrientation(SplitLayout.Orientation.VERTICAL);
        addThemeName(Theme.APP_STYLE.getName());
        addThemeName(Theme.SMALL.getName());
        this.setSizeFull();
    }

    public SplitterUtils orientation(SplitLayout.Orientation orientation) {
        this.setOrientation(orientation);
        return this;
    }

    public SplitterUtils position(double position) {
        this.setSplitterPosition(position);
        return this;
    }

    public SplitterUtils theme(Theme... theme) {
        Arrays.stream(theme).forEach(e -> this.addThemeName(e.getName()));
        return this;
    }

    public SplitterUtils locked() {
        addThemeName("locked");
        return this;
    }
}

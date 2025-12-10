package org.pacos.base.window;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;

public class WindowHeader extends Div {

    private final Div left = new Div();
    private final Div right = new Div();
    private final Div center = new Div();
    private final DesktopWindow desktopWindow;
    private final UISystem uiSystem;
    private final Span titleLabel;
    private Button expandBtn;
    private Button minimizeBtn;

    public WindowHeader(String title, DesktopWindow desktopWindow, UISystem uiSystem) {
        this.uiSystem = uiSystem;
        left.setClassName("header-left");
        right.setClassName("header-right");
        center.setClassName("header-center");


        setClassName("header");
        this.titleLabel = new Span(title);
        titleLabel.setClassName("header-label");
        getStyle().set("display", "contents");
        add(left, center, right);
        addToCenter(titleLabel);
        this.desktopWindow = desktopWindow;

        addToLeftSide(buildCloseButton());
        addExpandButton(buildExpandButton());
        addMinimizeButton(buildMinimizeButton());
    }

    public void addToLeftSide(Component component) {
        left.add(component);
    }

    public void addToCenter(Component component) {
        center.add(component);
    }

    public void addToRightSide(Component component) {
        right.add(component);
    }

    public void addExpandButton(Button expandBtn) {
        this.expandBtn = expandBtn;
        addToRightSide(expandBtn);
    }

    public void addMinimizeButton(Button minimizeBtn) {
        this.minimizeBtn = minimizeBtn;
        addToRightSide(minimizeBtn);
    }

    public void onHeaderDblClick() {
        expandEvent(expandBtn);
    }
    private Button buildExpandButton() {
        Button btn = new Button(icon(VaadinIcon.EXPAND));
        btn.addClickListener(e -> expandEvent(btn));
        btn.setClassName("header-btn btn-expand");
        return btn;
    }

    private void expandEvent(Button btn) {
        expandOrRestore();
        if (desktopWindow.getExpandInfo().isExpanded()) {
            btn.setIcon(icon(VaadinIcon.COMPRESS));
        } else {
            btn.setIcon(icon(VaadinIcon.EXPAND));
        }
    }

    private void expandOrRestore() {
        desktopWindow.getExpandInfo().expand();
        if (desktopWindow.getExpandInfo().isExpanded()) {
            desktopWindow.addThemeName("no-radius");
        } else {
            desktopWindow.removeThemeName("no-radius");
        }
    }

    private Button buildMinimizeButton() {
        var btn = new Button(icon(VaadinIcon.MINUS),
                e -> uiSystem.getWindowManager().showOrHideAlreadyCreatedWindow(desktopWindow, false));
        btn.setClassName("header-btn btn-minimize");
        return btn;
    }

    private Button buildCloseButton() {
        var btn = new Button(icon(VaadinIcon.CLOSE), e -> uiSystem.notify(ModuleEvent.MODULE_SHUTDOWN, desktopWindow));
        btn.setClassName("header-btn btn-close");
        return btn;
    }

    private static Icon icon(VaadinIcon vi) {
        var icon = new Icon(vi);
        icon.setColor("var(--app-header)");
        return icon;
    }

    public Div getLeft() {
        return left;
    }

    public Div getRight() {
        return right;
    }

    public Div getCenter() {
        return center;
    }

    public Button getExpandBtn() {
        return expandBtn;
    }

    public Button getMinimizeBtn() {
        return minimizeBtn;
    }

    public Span getTitleLabel() {
        return titleLabel;
    }
}

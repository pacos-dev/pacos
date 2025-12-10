package org.pacos.base.utils.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.IconFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.pacos.base.component.Theme;
import org.pacos.base.security.Permission;
import org.pacos.base.session.UserSession;

public class ButtonUtils extends Button {

    public ButtonUtils(String text) {
        super(text);
    }

    public ButtonUtils(IconFactory icon) {
        super(icon.create());
    }

    public ButtonUtils(Component icon) {
        super(icon);
    }

    public ButtonUtils(String text, Component icon) {
        super(text, icon);
    }

    public ButtonUtils(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text, clickListener);
    }

    public ButtonUtils(Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(icon, clickListener);
    }

    public ButtonUtils(String text, Component icon,
            ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text, icon, clickListener);
    }

    public ButtonUtils() {
    }

    public ButtonUtils infoLayout() {
        addThemeName("info_l");
        return this;
    }

    public ButtonUtils tabsheetBtn() {
        addThemeName("tab");
        withVariants(ButtonVariant.LUMO_ICON);
        return this;
    }

    public ButtonUtils errorLayout() {
        addThemeName("error_l");
        return this;
    }

    public ButtonUtils successLayout() {
        addThemeName("success_l");
        return this;
    }

    public ButtonUtils primaryLayout() {
        addThemeName("primary_l");
        return this;
    }

    public ButtonUtils tabCloseBtn() {
        setIcon(IconUtils.tetiaryIcon(VaadinIcon.CLOSE));
        addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        addThemeName(Theme.TAB_CLOSE.getName());
        return this;
    }

    public ButtonUtils withVariants(ButtonVariant... variants) {
        super.addThemeVariants(variants);
        return this;
    }

    public ButtonUtils withThemes(String... themes) {
        super.addThemeNames(themes);
        return this;
    }

    public ButtonUtils withClickListener(ComponentEventListener<ClickEvent<Button>> clickListener) {
        super.addClickListener(clickListener);
        return this;
    }

    public ButtonUtils withClassName(String className) {
        addClassName(className);
        return this;
    }

    public ButtonUtils withEnabled(boolean enabled) {
        setEnabled(enabled);
        return this;
    }

    public ButtonUtils withStyle(String property, String value) {
        getStyle().set(property, value);
        return this;
    }

    public ButtonUtils floatRight() {
        withStyle("float", "right");
        return this;
    }

    public ButtonUtils hidden() {
        setVisible(false);
        return this;
    }

    public ButtonUtils withWidth(String width) {
        setWidth(width);
        return this;
    }

    public ButtonUtils withHeight(String height) {
        setHeight(height);
        return this;
    }

    public ButtonUtils withTooltip(String text) {
        setTooltipText(text);
        return this;
    }

    public ButtonUtils withVisibleForPermission(Permission permission) {
        setVisible(UserSession.getCurrent().hasPermission(permission));
        return this;
    }

    public ButtonUtils withEnabledForPermission(Permission permission) {
        boolean allowed = UserSession.getCurrent().hasPermission(permission);
        setEnabled(allowed);
        if (!allowed) {
            setTooltipText("You do not have permission to access this button");
        }
        return this;
    }
}

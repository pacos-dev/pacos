package org.pacos.base.component.icon;

import java.util.Locale;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.icon.IconFactory;

/**
 * Auto generated class by Vaadin Icon
 */
@JsModule("./icons/my-icons-svg.js")
public enum IconsSvg implements IconFactory {
    FOLDER, SERVER, SQLFILE;

    public Icon create() {
        return new Icon(this.name().toLowerCase(Locale.ENGLISH).replace('_', '-').replaceAll("^-", ""));
    }

    public static final class Icon extends com.vaadin.flow.component.icon.Icon {
        Icon(String icon) {
            super("my-icons-svg", icon);
        }
    }
}
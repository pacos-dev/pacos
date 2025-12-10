package org.pacos.base.utils.component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import org.pacos.base.component.Theme;

@SuppressWarnings("squid:S110")
public class TextFieldUtils extends TextField {

    public static TextFieldUtils configureSearchField(String placeholder) {
        TextFieldUtils searchField = new TextFieldUtils();
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.addThemeName(Theme.NO_BORDER.getName());
        searchField.setClassName("search-filter");
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder(placeholder);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        return searchField;
    }

    public static TextFieldUtils withPlaceholder(String placeholder) {
        TextFieldUtils field = new TextFieldUtils();
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.addThemeName(Theme.NO_BORDER.getName());
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        return field;
    }

    public TextFieldUtils() {
    }

    public TextFieldUtils(String label) {
        super(label);
    }

    public TextFieldUtils withClassName(String className) {
        addClassName(className);
        return this;
    }

    public TextFieldUtils withWidth(int width) {
        setWidth(width, Unit.PIXELS);
        return this;
    }

    public TextFieldUtils withFullWidth() {
        setWidthFull();
        return this;
    }

    public TextFieldUtils withLabel(String label) {
        setLabel(label);
        return this;
    }
}

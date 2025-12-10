package org.pacos.base.utils.component;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import org.pacos.base.security.Permission;
import org.pacos.base.session.UserSession;

public class CheckboxUtils extends Checkbox {

    public CheckboxUtils(String labelText, boolean initialValue,
            ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        super(labelText, initialValue, listener);
    }

    public CheckboxUtils() {
        super();
    }

    public CheckboxUtils(String labelText) {
        super(labelText);
    }

    public CheckboxUtils withStyle(String prop, String val) {
        getStyle().set(prop, val);
        return this;
    }

    public CheckboxUtils withValueChangeListener(
            HasValue.ValueChangeListener<? super ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        super.addValueChangeListener(listener);
        return this;
    }

    public CheckboxUtils withWidth(String with) {
        setWidth(with);
        return this;
    }

    public CheckboxUtils withSelected() {
        setValue(true);
        return this;
    }

    public CheckboxUtils withEnabledForPermission(Permission permission) {
        boolean allowed = UserSession.getCurrent().hasPermission(permission);
        setEnabled(allowed);
        if (!allowed) {
            setTooltipText("You do not have permission to access this button");
        }
        return this;
    }

}

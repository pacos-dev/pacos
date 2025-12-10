package org.pacos.base.utils.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.UnorderedList;

public class UlUtils extends UnorderedList {

    public static UlUtils ofClass(String className) {
        UlUtils li = new UlUtils();
        li.setClassName(className);
        return li;
    }

    public UlUtils withComponents(Component... components) {
        super.add(components);
        return this;
    }
}

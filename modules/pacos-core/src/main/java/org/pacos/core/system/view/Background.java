package org.pacos.core.system.view;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.html.Image;
import org.pacos.base.utils.component.ImageUtils;

public class Background {

    private Background() {
    }

    static void configure(HasComponents component) {
        Image background = new ImageUtils("img/wallpaper/wallpaper3.jpg");
        background.setVisible(false);
        component.add(background);
        component.getElement().getStyle().set("background-image", "url(" + background.getSrc() + ")");
        component.getElement().getClassList().add("background");

    }
}

package org.pacos.core.component.plugin.view.plugin;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import org.pacos.core.component.plugin.dto.PluginDTO;

public class PluginRowLine extends Div {

    public PluginRowLine(PluginDTO module) {
        setClassName("m-info");
        Span block = new Span();
        block.addClassName("m-block");
        block.addClassName("m-block-image");
        add(block);
        if (module.getIcon() != null) {
            block.add(new Image(module.getIcon(), "module-icon"));
        } else {
            block.add(new Image("icons/noimage.png", "no-image"));
        }
        block = new Span();
        block.addClassName("m-block");
        block.getStyle().set("width", "inherit");
        add(block);

        Div nameBlock = new Div();
        Span nameTitle = new Span(module.getName());
        nameTitle.addClassName("m-bold");
        nameBlock.add(nameTitle);

        block.add(nameBlock);

        Div detailsBlock = new Div();
        detailsBlock.addClassName("inline");
        detailsBlock.addClassName("details");
        detailsBlock.setText(module.getVersion() + " | " + module.getAuthor());
        block.add(detailsBlock);
    }
}

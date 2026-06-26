package org.pacos.core.component.ai.view;

import org.pacos.base.component.NoContent;
import org.pacos.base.window.DialogJS;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ChatWindow extends Dialog {

    public ChatWindow() {
        getFooter().removeAll();
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.VISUAL);
        setWidth(350, Unit.PIXELS);
        setHeight(277, Unit.PIXELS);
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        var globalGridNoContent = new NoContent("You don't have configured global variables", " Let's change that!")
                .paddingTop(20);
        add(globalGridNoContent);
        DialogJS.setAbsolutePosition("unset", "0", "0", this);
    }

    public void open() {
        getFooter().removeAll();
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.MODELESS);
        setWidth(400, Unit.PIXELS);
        setHeight(220, Unit.PIXELS);
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        DialogJS.setAbsolutePosition("unset", "0", "0", this);
        super.open();

        removeAll();

        VerticalLayout aiSidebar = new VerticalLayout();
        aiSidebar.addClassName("ai-sidebar"); // Stylizacja w CSS
        aiSidebar.setVisible(false);

        MessageList list = new MessageList();
        MessageInput input = new MessageInput();
        input.addSubmitListener(e -> list.addItem(new MessageListItem(e.getValue())));
        add(new H3("Pacos AI"), list, input);

    }

}

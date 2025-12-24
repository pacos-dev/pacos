package org.pacos.core.component.variable.view.help;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.variable.view.config.VariablePluginHelpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.addons.variablefield.field.VariableTextField;

import java.util.List;

@Component
@Scope("prototype")
public class VariablePluginHelp extends DesktopWindow {

    private static final String HELP_TEXT_1 = """
            <b>Variable plugin</b><br>
            The variable plugin is used to manage the current configuration context.
            <br><br>
            To configure a collection list or global variable list, go to the variable management panel using the 
            "Manage collections" button or search for an application called 'Variables'.
            <br><br>
            <b>Active collection</b><br>
            Use the drop-down list to select the currently active collection. Once a collection is selected, the 
            variables are automatically reloaded throughout the system. 
            Only one active collection can be selected at a time.
            <br>
            Global variables with the same name are overwritten by variables from the currently active collection.
            <br><br>
            <b>Dynamic edition</b><br>
            The current value of the variable can be changed dynamically from the variable plugin. 
            Double-click the line with the variable to edit its current value.
            <br>
            Disabled variables are not shown in the variable tooltip window.
              """;

    @Autowired
    public VariablePluginHelp(VariablePluginHelpConfig moduleConfig) {
        super(moduleConfig);
        VerticalLayout content = new VerticalLayout();
        Div acc1 = new Div();
        acc1.getElement().setProperty("innerHTML", HELP_TEXT_1);
        content.add(new Paragraph(acc1));
        content.add(new VariableTextField(List.of(ScopeDefinition.SYSTEM)));

        add(content);
        getWindowHeader().getExpandBtn().setVisible(false);
        setWidth(600, Unit.PIXELS);
        setHeight(450, Unit.PIXELS);

        setPosition("100px", "40px");
    }
}

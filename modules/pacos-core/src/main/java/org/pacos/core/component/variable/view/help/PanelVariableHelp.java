package org.pacos.core.component.variable.view.help;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.pacos.base.session.UserSession;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.base.window.DesktopWindow;
import org.pacos.core.component.variable.view.config.VariableHelpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.addons.variablefield.field.VariableTextField;

@Component
@Scope("prototype")
public class PanelVariableHelp extends DesktopWindow {

    private static final String HELP_TEXT_1 = """
            Variables are used throughout the system to dynamically change values, i.e. change the context/behavior of individual applications/configurations.
            <br><br>
            <b>Variables</b><br>
            The Variables panel allows you to create and manage collections of variables.
            <br>
            Variables are used throughout the system and enable dynamic context changes.
            <br>
            There are several levels of variables in the system, each of which has a different priority. 
            User variables have the highest priority and in the case of a variable with the same name, 
            the user variable overwrites its value. Below is a list of priorities - the highest at the top:
            <br><br><br>
            <b>Variable priority</b><br>       
            - Active collection of user variables<br>
            - Global collection of user variables<br>
            - Module/application variables (if the module provides variables)<br>
            - System variables<br>
            <br><br>            
            Users can define an unlimited number of variable collections. There can only be one active collection at a time.
            <br>
            The exception is the collection called GLOBAL which is always active. 
            <br>
            It cannot be deleted and is active at all times within the user's session. 
            <br>
            Variables in the GLOBAL collection extend the active collection. 
            <br>
            Variables with the same name from the GLOBAL collection are overwritten by user collection variables.
            <br><br> 
            <b>Use of variables</b><br>              
            Each variable has two values: inital and current. If current is not set, initial will be used.
            <br>            
            Variables can be entered in fields marked with the {v} icon.
            <br>
            Below, there is a variable field configured for the current user session. 
            It contains system variables and currently configured user variables.
            Try them out by changing the configuration and adding variables.
             """;

    private static final String HELP_TEXT_2 = """        
            A tooltip window containing a list of possible variables to use will be displayed if the user enters 
            the '{' character or uses the ctrl + space keyboard shortcut in the input field. 
            Variables that are disabled (have the enabled field set to false) will not be displayed in the 
            tooltip window.
            <br><br>            
            Variables in the tooltip window are displayed with the highest priority from the top.
            """;

    @Autowired
    public PanelVariableHelp(VariableHelpConfig moduleConfig) {
        super(moduleConfig);
        VerticalLayout content = new VerticalLayout();
        Div acc1 = new Div();
        acc1.getElement().setProperty("innerHTML", HELP_TEXT_1);
        content.add(new Paragraph(acc1));
        content.add(new VariableTextField(ScopeDefinition.userScopes(UserSession.getCurrent().getUserId())));

        Div acc2 = new Div();
        acc2.getElement().setProperty("innerHTML", HELP_TEXT_2);
        content.add(acc2);

        add(content);

        setWidth(700, Unit.PIXELS);
        setHeight(750, Unit.PIXELS);

        getWindowHeader().getExpandBtn().setVisible(false);
        setPosition("100px", "40px");
    }
}

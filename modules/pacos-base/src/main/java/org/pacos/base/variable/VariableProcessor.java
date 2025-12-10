package org.pacos.base.variable;

import java.util.List;

import org.pacos.base.session.UserDTO;
import org.vaadin.addons.variablefield.data.Scope;

/**
 * Specifies access to the bean responsible for translating string lines with defined variables with specific scopes
 */
public interface VariableProcessor {

    /**
     * Replace all variables in given line based on passed scope list if user.
     * If the given user is null then only available app scope will be used for processing
     */
    String process(List<Scope> scopes, String line, UserDTO userDTO);

}

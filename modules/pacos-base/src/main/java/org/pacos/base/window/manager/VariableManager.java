package org.pacos.base.window.manager;

import java.io.Serializable;
import java.util.List;

import org.vaadin.addons.variablefield.data.Scope;
import org.vaadin.addons.variablefield.data.Variable;
import org.vaadin.addons.variablefield.provider.VariableProvider;

public interface VariableManager extends Serializable {

    void addProvider(VariableProvider variableProvider);

    void removeProvider(VariableProvider variableProvider);

    /**
     * Updates variable definition on frontend for defined scope if it was already sent before
     */
    void updateVariable(Scope scope, Variable variable);

    /**
     * Removes variable definition from frontend from defined scope
     */
    void removeVariable(Scope scope, Variable variable);

    /**
     * Refresh all variables for given scope
     * If force is true, then variable list will reload on backend and send again to frontend
     * If force is false, then cached variable list handling on backend will be used for reloading on UI
     */
    void updateVariablesForScope(Scope scope, boolean force);

    /**
     * Set static list of variables for scope. List is send immediately to the client
     */
    void setVariablesForScope(Scope scope, List<Variable> variables);

    /**
     * Remove scope and all assigned variables
     */
    void removeScope(Scope scope);
}

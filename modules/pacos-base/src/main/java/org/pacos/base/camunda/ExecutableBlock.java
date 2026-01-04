package org.pacos.base.camunda;

import java.util.List;

import org.vaadin.addons.variablefield.data.Scope;

public interface ExecutableBlock<T> {

    BlockMetadata basicData();

    BlockFormHandler<T> blockForm();

    /**
     * Execution called by camunda.
     *
     * @param processVariableManager - variable manager from current execution
     * @param bean            - bean of custom form. Can be null if not specified
     * @param scopes          - variable scope from pacos system for current execution
     */
    void execute(ProcessVariableManager processVariableManager,T bean, List<Scope> scopes) throws Exception;

    default void execute(ProcessVariableManager processVariableManager, List<Scope> scopes, String jsonModel) throws Exception {
        execute(processVariableManager, blockForm().readModel(jsonModel).orElse(null), scopes);
    }
}

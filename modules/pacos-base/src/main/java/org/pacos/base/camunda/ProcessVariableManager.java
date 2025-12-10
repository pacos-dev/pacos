package org.pacos.base.camunda;

public interface ProcessVariableManager {

    Object getVariable(String name);

    /**
     * Aliad from basic block configuration will be added as a prefix to the given name
     * @param name - variable name
     * @param value - serializable variable value
     */
    void setVariable(String name, Object value);

}

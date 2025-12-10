package org.pacos.base.camunda;

/**
 * Basic block data
 */
public interface BlockMetadata {

    /**
     * @return name displayed on UI
     */
    String name();

    /**
     * @return This name will be used by camunda to identify the bean on which the execute method will be called.
     * To correctly bind serviceTask to a specific bean, fill in the field
     * camundaDelegateExpresion = ${delegates['camundaDelegateName']}
     * This field must be unique in the whole application, otherwise will be overwritten by another implementation
     */
    String camundaDelegateName();

    /**
     * The group in which this block will be placed. A tree will be created in the view based on this value.
     */
    String[] group();

    /**
     * Returns a list of variable names that will be generated during block processing. This is a helper list that will
     * be included in the view when displaying the block.
     */
    ResultVariable[] resultVariables();
}

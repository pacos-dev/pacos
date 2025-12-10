package org.pacos.base.camunda;

/**
 * Used to resolve expected Bpmn error (check BpmnError)
 */
public class PacosBoundaryError extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public PacosBoundaryError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

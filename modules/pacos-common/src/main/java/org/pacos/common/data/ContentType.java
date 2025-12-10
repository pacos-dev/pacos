package org.pacos.common.data;

public enum ContentType {

    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml"),
    TEXT_XML("text/xml"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    MULTIPART_MIXED("multipart/mixed");

    private final String type;

    ContentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

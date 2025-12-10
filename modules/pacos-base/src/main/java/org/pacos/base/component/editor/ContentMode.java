package org.pacos.base.component.editor;

/**
 * Define how code mirror should format displayed content
 */
public enum ContentMode {

    TEXT("Text"),
    JAVASCRIPT("Javascript"),
    JSON("JSON"),
    HTML("HTML"),
    XML("XML"),
    JAVA("JAVA"),
    SQL("SQL");

    private final String name;

    ContentMode(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return name;
    }
}

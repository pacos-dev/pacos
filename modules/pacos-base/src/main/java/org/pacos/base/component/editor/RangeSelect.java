package org.pacos.base.component.editor;

/**
 * Represents range selection done by user on codeMirror
 */
public record RangeSelect(String value, int rangeFrom, int rangeTo) {

}

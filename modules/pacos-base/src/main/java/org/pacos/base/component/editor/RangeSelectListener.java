package org.pacos.base.component.editor;

/**
 * Called when the client returns a value upon request from the server.
 * Method is called asynchronously by the server
 */
@FunctionalInterface
public interface RangeSelectListener {

    void rangeSelect(RangeSelect rangeSelect);
}

/**
 * This package contains implementation for code mirror
 * CodeMirror has been configured to be compatible with the Vaadin binder. Therefore, codemirror can be used as a form field
 *
 * <p>Example of usage</p>
 * <pre>{@code
 * var editor = new NativeCodeMirror();
 * layout.add(editor);
 * }
 * </pre>
 * CodeMirror supports handling pacos variables. To configure, just pass the Scope list
 * <pre>{@code
 * Scope scope = new Scope("test", 0, 'G', "#4642345");
 * var editor = new NativeCodeMirror(List.of(scope));
 * layout.add(editor);
 * }
 * </pre>
 */
package org.pacos.base.component.editor;
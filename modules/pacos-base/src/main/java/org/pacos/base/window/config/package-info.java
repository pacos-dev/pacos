/**
 * This package contains classes for configuring a window. All main application windows have a configuration that inherits from
 * {@link org.pacos.base.window.config.WindowConfig}.
 * Based on this configuration, the pacos is able to create a window and dynamically manage it.
 * The basic configuration can be extended with additional functions by expanding it with the implementation of additional interfaces.
 *
 * <p>Example of usage</p>
 * <pre>{@code
 * public class MyWindowConfig implements WindowConfig,FileExtensionHandler {
 *     ...
 * }</pre>
 *
 */
package org.pacos.base.window.config;
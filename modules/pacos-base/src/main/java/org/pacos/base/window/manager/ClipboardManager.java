package org.pacos.base.window.manager;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * Allows to read clipboard value from the client side
 */
public interface ClipboardManager extends Serializable {
    /**
     * Start reading clipboard value. This is async call that can't be used idrectly in vaadin session thread.
     * Example of usage:
     * //read clipboard in vaadin session thread
     * CompletableFuture<String> stringCompletableFuture = UISystem.getCurrent().getClipboardManager().readClipboard();
     * UI ui = UI.getCurrent();
     * //read response without blocking vaadin session
     * CompletableFuture.runAsync(() -> {
     * try {
     * String value = stringCompletableFuture.get(5, TimeUnit.SECONDS);
     * //access to vaadin session inside thread
     * ui.access(() -> NotificationUtils.show(value));
     * } catch (InterruptedException | ExecutionException | TimeoutException e) {
     * throw new RuntimeException(e);
     * }
     * });
     */
    CompletableFuture<String> readClipboard();
}

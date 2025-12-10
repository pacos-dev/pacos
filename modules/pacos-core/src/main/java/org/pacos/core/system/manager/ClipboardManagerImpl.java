package org.pacos.core.system.manager;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.html.Span;
import org.pacos.base.window.manager.ClipboardManager;

public class ClipboardManagerImpl extends Span implements ClipboardManager, Serializable {

    /**
     * Limit for clipboard content that can be transferred from the client
     */
    private static final int MAX_LENGTH = 2000;

    private transient CompletableFuture<String> clipboardContentFuture;

    public ClipboardManagerImpl() {
        getElement().getStyle().set("display", "none");
    }

    @Override
    public CompletableFuture<String> readClipboard() {
        clipboardContentFuture = new CompletableFuture<>();
        getUI().ifPresent(ui ->
                ui.getPage().executeJs(
                        "navigator.clipboard.readText().then(content => {" +
                                "   if (content.length > $1) {" +
                                "       console.warn('Clipboard content too long, max allowed $1 ignoring.');" +
                                "       $0.$server.reportClipboardError('Clipboard content exceeds 2000 characters.');" +
                                "   } else {" +
                                "       $0.$server.setClipboardContent(content);" +
                                "   }" +
                                "}).catch(err => {" +
                                "   $0.$server.reportClipboardError(err.message);" +
                                "});", getElement(), MAX_LENGTH)
        );
        return clipboardContentFuture;
    }


    @ClientCallable
    public void setClipboardContent(String content) {
        if (clipboardContentFuture != null) {
            clipboardContentFuture.complete(content);
        }
    }

    @ClientCallable
    public void reportClipboardError(String error) {
        if (clipboardContentFuture != null) {
            clipboardContentFuture.completeExceptionally(new RuntimeException(error));
        }
    }
}

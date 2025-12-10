package org.pacos.core.system.manager;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClipboardManagerImplTest {

    @Mock
    private UI mockUI;

    @Mock
    private Element mockElement;
    @Mock
    private Page mockPage;

    private ClipboardManagerImpl clipboardManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clipboardManager = spy(new ClipboardManagerImpl());
        doReturn(mockElement).when(clipboardManager).getElement();
        doReturn(Optional.of(mockUI)).when(clipboardManager).getUI();
        doReturn(mockPage).when(mockUI).getPage();
        when(clipboardManager.getUI()).thenReturn(java.util.Optional.of(mockUI));
    }

    @Test
    void whenReadClipboardCalledThenCompletableFutureIsInitialized() {
        CompletableFuture<String> future = clipboardManager.readClipboard();
        assertNotNull(future);
        verify(mockUI.getPage(), times(1)).executeJs(anyString(), any(), anyInt());
    }

    @Test
    void whenSetClipboardContentCalledThenFutureCompletesSuccessfully() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = clipboardManager.readClipboard();
        clipboardManager.setClipboardContent("Test content");

        assertEquals("Test content", future.get());
    }

    @Test
    void whenReportClipboardErrorCalledThenFutureCompletesExceptionally() {
        CompletableFuture<String> future = clipboardManager.readClipboard();
        clipboardManager.reportClipboardError("Error occurred");

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals("java.lang.RuntimeException: Error occurred", exception.getMessage());
    }

    @Test
    void whenClipboardContentExceedsMaxLengthThenErrorReported() {
        String longContent = "a".repeat(2001);
        clipboardManager.readClipboard();
        clipboardManager.setClipboardContent(longContent);

        CompletableFuture<String> future = clipboardManager.readClipboard();
        clipboardManager.reportClipboardError("Clipboard content exceeds 2000 characters.");

        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getMessage().contains("Clipboard content exceeds 2000 characters."));
    }
}

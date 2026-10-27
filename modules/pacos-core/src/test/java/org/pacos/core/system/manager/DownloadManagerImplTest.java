package org.pacos.core.system.manager;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;

class DownloadManagerImplTest {

    @Test
    void whenInitializeManagerThenDownloadAttributeIsSetToTrue() {
        DownloadManagerImpl manager = new DownloadManagerImpl();

        assertTrue(manager.getElement().hasAttribute("download"));
    }

    @Test
    void whenStartDownloadThenTriggerClickOnElement() {
        DownloadManagerImpl manager = spy(new DownloadManagerImpl());
        Element element = mock(Element.class);
        doReturn(element).when(manager).getElement();

        manager.startDownloading(mock(DownloadHandler.class));

        verify(element).callJsFunction("click");
    }

    @Test
    @SuppressWarnings("removal")
    void whenStartDownloadWithStreamResourceThenTriggerClickOnElement() {
        DownloadManagerImpl manager = spy(new DownloadManagerImpl());
        Element element = mock(Element.class);
        doReturn(element).when(manager).getElement();

        manager.startDownloading(mock(StreamResource.class));

        verify(element).callJsFunction("click");
    }
}

package org.pacos.core.system.manager;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class DownloadManagerImplTest {

    @Test
    void whenInitializeManagerThenDownloadAttributeIsSetToTrue() {
        DownloadManagerImpl manager = new DownloadManagerImpl();
        //then
        assertTrue(manager.getElement().hasAttribute("download"));
    }

    @Test
    void whenStartDownloadThenTriggerClickOnElement() {
        DownloadManagerImpl manager = spy(new DownloadManagerImpl());
        Element element = mock(Element.class);
        doReturn(element).when(manager).getElement();
        //when
        manager.startDownloading(mock(StreamResource.class));
        //then
        verify(element).callJsFunction("click");
    }
}
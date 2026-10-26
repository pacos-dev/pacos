package org.pacos.core.system.manager;

import java.io.Serializable;

import org.pacos.base.window.manager.DownloadManager;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;

public class DownloadManagerImpl extends Anchor implements DownloadManager, Serializable {

    public DownloadManagerImpl() {
        setText("auto_download_link");
        getElement().getStyle().set("display", "none");
        getElement().setAttribute("download", true);
    }

    @Override
    public void startDownloading(DownloadHandler downloadHandler) {
        setHref(downloadHandler);
        getElement().callJsFunction("click");
    }

    @Deprecated(forRemoval = true)
    @Override
    public void startDownloading(StreamResource resource) {
        setHref(resource);
        getElement().callJsFunction("click");
    }



}

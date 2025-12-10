package org.pacos.core.system.manager;

import java.io.Serializable;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.server.StreamResource;
import org.pacos.base.window.manager.DownloadManager;

public class DownloadManagerImpl extends Anchor implements DownloadManager, Serializable {

    public DownloadManagerImpl() {
        setText("auto_download_link");
        getElement().getStyle().set("display", "none");
        getElement().setAttribute("download", true);
    }

    public void startDownloading(StreamResource resource) {
        setHref(resource);
        getElement().callJsFunction("click");
    }
}

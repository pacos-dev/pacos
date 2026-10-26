package org.pacos.base.window.manager;

import java.io.Serializable;

import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;

/**
 * Allows to initiate download process on client side
 */
public interface DownloadManager extends Serializable {

    /**
     * Starts download process for given resources
     */
    void startDownloading(DownloadHandler downloadHandler);

    @Deprecated(forRemoval = true)
    void startDownloading(StreamResource resource);
}

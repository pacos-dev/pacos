package org.pacos.base.utils.component;

import java.io.File;
import java.nio.file.Path;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.streams.DownloadHandler;

public class ImageUtils extends Image {

    public ImageUtils(String srcLocation) {
        super(srcLocation, "");
    }

    public ImageUtils(String src, String alt) {
        super(src, alt);
    }

    public ImageUtils(DownloadHandler resource, String alt) {
        super(resource, alt);
    }

    public ImageUtils withWidth(int width) {
        setWidth(width, Unit.PIXELS);
        return this;
    }

    public ImageUtils withHeight(int height) {
        setHeight(height, Unit.PIXELS);
        return this;
    }

    public ImageUtils withStyle(String property, String value) {
        getStyle().set(property, value);
        return this;
    }

    public static ImageUtils fromLocation(Path path, String alt) {
        File file = path.toFile();
        DownloadHandler resource = DownloadHandler.forFile(file);
        return new ImageUtils(resource, alt);
    }
}

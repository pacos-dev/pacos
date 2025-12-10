package org.pacos.base.component.filepicker;

import java.io.Serializable;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import org.pacos.base.component.icon.MimeIconFactory;
import org.pacos.base.file.FileInfo;
import org.pacos.base.utils.component.SpanUtils;

public record FilePickerRow(FileInfo fileInfo) implements Serializable {

    public Span getLabel() {
        Icon image = MimeIconFactory.getForMimeType(getType());
        if (fileInfo.getFile().isHidden()) {
            image.getStyle().set("opacity", "0.5");
        }
        image.addClassName("ex-img");
        return new SpanUtils().withComponents(image, SpanUtils.ofClass("ex-label").withText(getName()));
    }

    public String getType() {
        if (fileInfo.getFile().isDirectory()) {
            return "Directory";
        }
        final String[] split = getName().split("\\.");
        return split.length > 1 ? split[split.length - 1] : "";
    }

    public String getName() {
        return fileInfo.getName();
    }

    public Stream<FilePickerRow> getChild() {
        return fileInfo.getChild().stream().map(FilePickerRow::new);
    }

    public boolean isDirectory() {
        return fileInfo.isDirectory();
    }

}

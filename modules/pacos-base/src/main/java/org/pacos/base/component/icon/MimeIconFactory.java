
package org.pacos.base.component.icon;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;

public enum MimeIconFactory {

    ZIP(MimeIcon.ZIP, "zip", "tar", "gz"),
    FOLDER(MimeIcon.FOLDER, "directory"),
    TXT(MimeIcon.TXT, "txt", "doc"),
    XML(MimeIcon.XML, "xml", "properties"),
    JPG(MimeIcon.JPG, "jpg", "png", "jpeg"),
    LOG(MimeIcon.LOG, "log"),
    VIDEO(MimeIcon.LOG, "avi", "ogg", "mp4"),
    HTML(MimeIcon.HTML, "html"),
    KEY(MimeIcon.KEY, "pem", "pfx", "csr", "p12", "key", "cacrt"),
    CSV(MimeIcon.CSV, "csv"),
    GREEN(MimeIcon.GREEN, "json"),
    BPMN(MimeIcon.BPMN, "bpmn"),
    BLACK(MimeIcon.BLACK, "java");

    private static final Map<String, IconFactory> mimeMap = new HashMap<>();

    static {
        Arrays.stream(MimeIconFactory.values())
                .forEach(mime -> Arrays.stream(mime.mimeType).forEach(ext -> mimeMap.put(ext, mime.icon)));
    }

    private final IconFactory icon;
    private final String[] mimeType;

    MimeIconFactory(IconFactory icon, String... mimeType) {
        this.icon = icon;
        this.mimeType = mimeType;
    }

    public static Icon getForMimeType(String mime) {
        IconFactory factory = mimeMap.get(mime.toLowerCase(Locale.getDefault()));
        if (factory != null) {
            return factory.create();
        }
        return MimeIcon.BLANK.create();
    }

    public Icon create() {
        return icon.create();
    }
}

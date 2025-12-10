package org.pacos.base.component.icon;

import com.vaadin.flow.component.icon.Icon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MimeIconTest {

    @Test
    void whenBlackMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.BLACK.create();

        assertEquals("my-icons-mime:black", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenBlankMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.BLANK.create();

        assertEquals("my-icons-mime:blank", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenCsvMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.CSV.create();

        assertEquals("my-icons-mime:csv", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenFolderMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.FOLDER.create();

        assertEquals("my-icons-mime:folder", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenHtmlMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.HTML.create();

        assertEquals("my-icons-mime:html", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenJpgMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.JPG.create();

        assertEquals("my-icons-mime:jpg", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenKeyMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.KEY.create();

        assertEquals("my-icons-mime:key", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenLogMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.LOG.create();

        assertEquals("my-icons-mime:log", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenTxtMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.TXT.create();

        assertEquals("my-icons-mime:txt", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenVideoMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.VIDEO.create();

        assertEquals("my-icons-mime:video", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenXmlMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.XML.create();

        assertEquals("my-icons-mime:xml", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenZipMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.ZIP.create();

        assertEquals("my-icons-mime:zip", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenGreenMimeIconThenCreateReturnsCorrectIcon() {
        Icon icon = MimeIcon.GREEN.create();

        assertEquals("my-icons-mime:green", icon.getElement().getAttribute("icon"));
    }
}

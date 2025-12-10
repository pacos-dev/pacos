package org.pacos.base.component.icon;

import com.vaadin.flow.component.icon.Icon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MimeIconFactoryTest {

    @Test
    void whenGetForMimeTypeZipThenReturnsZipIcon() {
        Icon icon = MimeIconFactory.getForMimeType("zip");
        assertEquals("my-icons-mime:zip", icon.getElement().getAttribute("icon"));
        assertNotNull(MimeIconFactory.ZIP.create());
    }

    @Test
    void whenGetForMimeTypeTxtThenReturnsTxtIcon() {
        Icon icon = MimeIconFactory.getForMimeType("txt");
        assertEquals("my-icons-mime:txt", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenGetForMimeTypeXmlThenReturnsXmlIcon() {
        Icon icon = MimeIconFactory.getForMimeType("xml");
        assertEquals("my-icons-mime:xml", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenGetForMimeTypeJpgThenReturnsJpgIcon() {
        Icon icon = MimeIconFactory.getForMimeType("jpg");
        assertEquals("my-icons-mime:jpg", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenGetForMimeTypeLogThenReturnsLogIcon() {
        Icon icon = MimeIconFactory.getForMimeType("log");
        assertEquals("my-icons-mime:log", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenGetForMimeTypeVideoThenReturnsLogIcon() {
        Icon icon = MimeIconFactory.getForMimeType("avi");
        assertEquals("my-icons-mime:log", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenGetForMimeTypeHtmlThenReturnsHtmlIcon() {
        Icon icon = MimeIconFactory.getForMimeType("html");
        assertEquals("my-icons-mime:html", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenGetForMimeTypeCsvThenReturnsCsvIcon() {
        Icon icon = MimeIconFactory.getForMimeType("csv");
        assertEquals("my-icons-mime:csv", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenGetForMimeTypeGreenThenReturnsGreenIcon() {
        Icon icon = MimeIconFactory.getForMimeType("json");
        assertEquals("my-icons-mime:green", icon.getElement().getAttribute("icon"));
    }

    @Test
    void whenGetForMimeTypeUnknownThenReturnsBlankIcon() {
        Icon icon = MimeIconFactory.getForMimeType("unknown");
        assertEquals("my-icons-mime:blank", icon.getElement().getAttribute("icon"));
    }
}

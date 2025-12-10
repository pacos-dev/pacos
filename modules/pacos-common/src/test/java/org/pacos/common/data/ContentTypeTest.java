package org.pacos.common.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContentTypeTest {

    @Test
    void whenApplicationJsonThenGetTypeReturnsCorrectValue() {
        assertEquals("application/json", ContentType.APPLICATION_JSON.getType());
    }

    @Test
    void whenApplicationXmlThenGetTypeReturnsCorrectValue() {
        assertEquals("application/xml", ContentType.APPLICATION_XML.getType());
    }

    @Test
    void whenTextXmlThenGetTypeReturnsCorrectValue() {
        assertEquals("text/xml", ContentType.TEXT_XML.getType());
    }

    @Test
    void whenMultipartFormDataThenGetTypeReturnsCorrectValue() {
        assertEquals("multipart/form-data", ContentType.MULTIPART_FORM_DATA.getType());
    }

    @Test
    void whenMultipartMixedThenGetTypeReturnsCorrectValue() {
        assertEquals("multipart/mixed", ContentType.MULTIPART_MIXED.getType());
    }
}

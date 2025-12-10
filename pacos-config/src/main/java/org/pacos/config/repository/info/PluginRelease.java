package org.pacos.config.repository.info;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record PluginRelease(
        @JacksonXmlProperty(localName = "version")
        String version,

        @JacksonXmlElementWrapper(localName = "changes")
        @JacksonXmlProperty(localName = "changes")
        List<String> changes) {
}

package org.pacos.config.repository.info;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;


public record PluginRelease(
        @JacksonXmlProperty(localName = "version")
        String version,

        @JacksonXmlElementWrapper(localName = "changes")
        @JacksonXmlProperty(localName = "changes")
        List<String> changes) {
}

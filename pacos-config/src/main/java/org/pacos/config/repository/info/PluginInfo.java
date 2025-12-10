package org.pacos.config.repository.info;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Represents information about plugin stored in plugin_info.xml file
 */
@JacksonXmlRootElement(localName = "plugin")
public record PluginInfo(

        @JacksonXmlProperty(localName = "name")
        String name,

        @JacksonXmlProperty(localName = "version")
        String version,

        @JacksonXmlProperty(localName = "icon")
        String icon,

        @JacksonXmlProperty(localName = "author")
        String author,

        @JacksonXmlProperty(localName = "description")
        String description,

        @JacksonXmlElementWrapper(localName = "releases")
        @JacksonXmlProperty(localName = "releases")
        List<PluginRelease> releases) {

}

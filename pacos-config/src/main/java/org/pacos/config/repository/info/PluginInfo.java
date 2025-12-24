package org.pacos.config.repository.info;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;


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

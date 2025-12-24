package org.pacos.config.repository.info;

import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;


/**
 * Represents information about plugin stored in plugin_info.xml file
 */
@JsonRootName("plugin")
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

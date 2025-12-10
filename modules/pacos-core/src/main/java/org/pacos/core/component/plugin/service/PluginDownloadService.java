package org.pacos.core.component.plugin.service;

import org.pacos.config.repository.PomDependencyResolver;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for downloading artifacts and dependencies from the repository
 */
public class PluginDownloadService {

    private static final Logger LOG = LoggerFactory.getLogger(PluginDownloadService.class);

    private PluginDownloadService() {
    }

    public static PluginDTO downloadPlugin(AppRepository repository, AppArtifact artifact, PluginDTO pluginDTO) {
        if (!PomDependencyResolver.resolve(artifact, repository, "jar")) {
            pluginDTO.setErrMsg("Can't resolve jar for plugin " + artifact);
            return pluginDTO;
        } else {
            LOG.info("Downloaded plugin {}", artifact);
        }
        return pluginDTO;
    }

}

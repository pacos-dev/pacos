package org.pacos.config.repository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.data.AppRepositoryArtifact;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.config.repository.data.PluginConfiguration;
import org.pacos.config.repository.info.Plugin;
import org.pacos.config.repository.info.PluginInfo;
import org.pacos.config.repository.metadata.Metadata;
import org.pacos.config.repository.metadata.MetadataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetching latest module.json file from remote repository based on maven-metadata
 */
public final class RepositoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryClient.class);
    private static final AppArtifact MODULE_ARTIFACT = new AppArtifact("org.pacos.info", "module", null);

    private RepositoryClient() {

    }

    public static ModuleConfiguration loadModule() throws DependencyResolverException {
        String latestVersion = loadLatestVersion();
        return getModuleConfiguration(latestVersion);
    }

    private static String loadLatestVersion() {
        AppRepository pacosRepository = AppRepository.moduleRepo();
        LOG.info("Loading latest module version from '{}'", AppRepository.moduleRepo());
        AppRepositoryArtifact repositoryArtifact = new AppRepositoryArtifact(MODULE_ARTIFACT, pacosRepository);
        Metadata metadata = MetadataLoader.loadMetadata(repositoryArtifact);
        LOG.info("Found release module version '{}'", metadata.versioning().release());
        return metadata.versioning().release();
    }

    public static ModuleConfiguration getModuleConfiguration(String latestVersion) {
        if (latestVersion == null || latestVersion.isEmpty()) {
            latestVersion = loadLatestVersion();
        }
        LOG.info("Download pacos in version {}", latestVersion);
        AppArtifact newestArtifact = MODULE_ARTIFACT.forVersion(latestVersion);
        AppRepositoryArtifact appRepositoryArtifact = new AppRepositoryArtifact(newestArtifact, AppRepository.moduleRepo());
        return fetchModuleConfiguration(appRepositoryArtifact.getFileUrl("json"));
    }

    public static List<Plugin> loadPluginList(AppRepository repository) {

        LOG.info("Load plugin lists from repo {}", repository);

        AppArtifact artifact = new AppArtifact("org.pacos.info", "plugin", null);
        AppRepositoryArtifact repositoryArtifact = new AppRepositoryArtifact(artifact, repository);
        Metadata metadata = MetadataLoader.loadMetadata(repositoryArtifact);

        String latestVersion = metadata.versioning().release();

        AppArtifact newestPluginInfo = artifact.forVersion(latestVersion);
        repositoryArtifact = new AppRepositoryArtifact(newestPluginInfo, repository);
        PluginConfiguration configuration = RepositoryClient.requestPluginList(repositoryArtifact.getFileUrl("json"));
        return loadNewestVersionForPlugins(configuration, repository);

    }

    private static List<Plugin> loadNewestVersionForPlugins(PluginConfiguration configuration, AppRepository repository) {
        List<Plugin> pluginListWithVersion = new ArrayList<>();
        for (Plugin plugin : configuration.plugins()) {
            try {
                AppRepositoryArtifact repositoryArtifact = new AppRepositoryArtifact(plugin.toAppArtifact(), repository);
                Metadata metadata = MetadataLoader.loadMetadata(repositoryArtifact);
                String latestVersion = metadata.versioning().release();
                pluginListWithVersion.add(
                        new Plugin(plugin.groupId(), plugin.artifactName(), plugin.name(), latestVersion, plugin.author(), plugin.icon()));
            } catch (Exception e) {
                LOG.error("Error loading plugin version'{}'", plugin, e);
            }
        }
        return pluginListWithVersion;
    }

    public static PluginInfo loadInfo(AppRepositoryArtifact artifact) {
        String url = artifact.getFileUrl("xml");
        LOG.info("Load plugin info from : {}", url);
        try (InputStream inputStream = URI.create(url).toURL().openStream()) {
            XmlMapper xmlMapper = new XmlMapper();
            PluginInfo pluginInfoDTO = xmlMapper.readValue(inputStream.readAllBytes(), PluginInfo.class);
            LOG.debug("Received info about plugin: {}", pluginInfoDTO);
            return pluginInfoDTO;
        } catch (Exception e) {
            LOG.error("Error when reading info about plugin {}", artifact);
            throw new DependencyResolverException("Error when reading info about plugin", e);
        }
    }

    public static Metadata loadMetadata(AppRepositoryArtifact repositoryArtifact) {
        AppArtifact artifact = new AppArtifact(repositoryArtifact.artifact().groupId(),
                repositoryArtifact.artifact().artifactName(), null);
        return MetadataLoader.loadMetadata(new AppRepositoryArtifact(artifact, repositoryArtifact.repository()));
    }

    private static PluginConfiguration requestPluginList(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            String response = HttpFileLoader.getFileContent(url);

            return new Gson().fromJson(response, PluginConfiguration.class);
        } catch (IOException | URISyntaxException e) {
            LOG.error("Error while fetching plugin.json file from given url {}", urlString);
            throw new DependencyResolverException(e);
        }
    }

    private static ModuleConfiguration fetchModuleConfiguration(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            String response = HttpFileLoader.getFileContent(url);

            return new Gson().fromJson(response, ModuleConfiguration.class);
        } catch (IOException | RuntimeException | URISyntaxException e) {
            throw new DependencyResolverException("Error while fetching module.json file from given url " + urlString, e);
        }
    }
}

package org.pacos.core.component.plugin.manager.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to plugin jar
 */
public class PluginJar {

    private static final Logger LOG = LoggerFactory.getLogger(PluginJar.class);
    private final Path path;
    private URLClassLoader urlClassLoader;

    public PluginJar(PluginDTO plugin) {
        AppArtifact artifact = new AppArtifact(plugin.getGroupId(), plugin.getArtifactName(), plugin.getVersion());
        Path libPath = WorkingDir.getLibPath();
        this.path = libPath.resolve(artifact.getJarPath());
    }

    public boolean exists() {
        return path.toFile().exists();
    }

    public URLClassLoader getPluginClassLoader() throws MalformedURLException {
        if (urlClassLoader == null) {
            URL jarUrl = path.toUri().toURL();
            this.urlClassLoader = new URLClassLoader(new URL[]{jarUrl}, this.getClass().getClassLoader());
        }
        return urlClassLoader;
    }

    public void closeClassLoader() {
        if (urlClassLoader != null) {
            long closeStartedAt = System.nanoTime();
            LOG.info("Closing URLClassLoader for jar {}", path.toAbsolutePath());
            try {
                this.urlClassLoader.close();
                LOG.info("Closed URLClassLoader for jar {} in {} ms",
                        path.toAbsolutePath(), (System.nanoTime() - closeStartedAt) / 1_000_000);
            } catch (IOException e) {
                LOG.error("Unable to close URLClassLoader for jar {} after {} ms",
                        path.toAbsolutePath(), (System.nanoTime() - closeStartedAt) / 1_000_000, e);
            }
        }
    }

    public Path getLibPath() {
        return path;
    }

    @Override
    public String toString() {
        return "PluginJar{" +
                "path=" + path +
                '}';
    }
}

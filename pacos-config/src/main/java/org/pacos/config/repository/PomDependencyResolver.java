package org.pacos.config.repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PomDependencyResolver {
    private static final Logger LOG = LoggerFactory.getLogger(PomDependencyResolver.class);
    private static final String REPO_TYPE = "default";
    private static final RepositorySystem system = newRepositorySystem();
    private static final RepositorySystemSession session = newRepositorySystemSession(WorkingDir.getLibPath());

    private PomDependencyResolver() {

    }

    public static void resolve(List<AppArtifact> appArtifact) throws DependencyResolverException {
        AppRepository pacosRepo = AppRepository.moduleRepo();
        for (AppArtifact artifact : appArtifact) {
            LOG.info("Resolving pacos module '{}'", artifact.getJarFileName());
            if (!PomDependencyResolver.resolve(artifact, pacosRepo, "jar")) {
                throw new DependencyResolverException("Can't download dependency " + artifact.getJarFileName());
            }
        }
    }

    public static boolean resolve(AppArtifact appArtifact, AppRepository appRepository, String extension) {
        try {
            LOG.debug("Starting download {}", appArtifact);
            List<RemoteRepository> repositories = new ArrayList<>(transformRepository(appRepository));

            Artifact artifact = new DefaultArtifact(appArtifact.groupId(), appArtifact.artifactName(), null, extension, appArtifact.version());
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.setRepositories(repositories);
            ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
            return artifactResult.isResolved();
        } catch (ArtifactResolutionException e) {
            LOG.error("Cant resolve dependency {}", appArtifact, e);
            return false;
        }
    }

    private static List<RemoteRepository> transformRepository(AppRepository repo) {
        return List.of(new RemoteRepository.Builder(repo.id(), REPO_TYPE, repo.url()).build());
    }

    private static RepositorySystem newRepositorySystem() {
        var locator = MavenRepositorySystemUtils.newServiceLocator();

        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private static RepositorySystemSession newRepositorySystemSession(Path localRepositoryPath) {
        DefaultRepositorySystemSession defaultSystem = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(localRepositoryPath.toString());
        LocalRepositoryManager manager = PomDependencyResolver.system.newLocalRepositoryManager(defaultSystem, localRepo);
        defaultSystem.setLocalRepositoryManager(manager);

        return defaultSystem;
    }
}

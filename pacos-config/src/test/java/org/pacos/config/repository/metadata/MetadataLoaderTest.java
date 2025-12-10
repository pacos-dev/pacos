package org.pacos.config.repository.metadata;

import java.io.IOException;
import java.util.Objects;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import org.pacos.config.repository.DependencyResolverException;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.data.AppRepositoryArtifact;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataLoaderTest {

    @Test
    void whenFetchMetadataThenReturnCorrectVersion() throws IOException {
        WireMockServer wireMockServer = mockMetaData(getValidResponse());

        //when
        AppArtifact artifact = new AppArtifact("org.pacos.info", "module", null);
        AppRepository repository = new AppRepository("default", "http://localhost:" + wireMockServer.port() + "/repository/pacos-maven-repo");
        Metadata metadata = MetadataLoader.loadMetadata(new AppRepositoryArtifact(artifact, repository));
        //then
        assertNotNull(metadata);
        assertNotNull(metadata.versioning());
        assertEquals("1.0.0", metadata.versioning().release());
        assertEquals("1.0.0", metadata.versioning().versions().version().get(0));
    }

    @Test
    void whenCantFetchMetadataThenReturnNull() throws IOException {
        WireMockServer wireMockServer = mockMetaData(getValidResponse());

        //when
        AppArtifact artifact = new AppArtifact("org.pacos.info", "plugin", null);

        AppRepository repository = new AppRepository("default", "http://localhost:" + wireMockServer.port() + "/repository/pacos-maven-repo");
        var repoArtifact = new AppRepositoryArtifact(artifact, repository);
        assertThrows(DependencyResolverException.class, () -> MetadataLoader.loadMetadata(repoArtifact));
    }

    @Test
    void whenFetchResponseWithoutBasicDataThenValueIdIsNull() {
        String response = """
                <metadata modelVersion="1.1.0">
                 
                </metadata>
                """;
        WireMockServer wireMockServer = mockMetaData(response);

        //when
        AppArtifact artifact = new AppArtifact("org.pacos.info", "module", null);
        AppRepository repository = new AppRepository("default", "http://localhost:" + wireMockServer.port() + "/repository/pacos-maven-repo");
        Metadata metadata = MetadataLoader.loadMetadata(new AppRepositoryArtifact(artifact, repository));
        //then
        assertNotNull(metadata);
        assertNull(metadata.groupId());
        assertNull(metadata.artifactId());
        assertNotNull(metadata.versioning());
        assertNull(metadata.versioning().latest());
        assertNull(metadata.versioning().release());
        assertNull(metadata.versioning().lastUpdated());
        assertNotNull(metadata.versioning().versions());
        assertTrue(metadata.versioning().versions().version().isEmpty());
    }


    @Test
    void whenServerNotAccessibleThenReturnNull() {
        AppArtifact artifact = new AppArtifact("org.pacos.info", "plugin", null);
        AppRepository repository = new AppRepository("default", "http://localhost:54645/repository/pacos-maven-repo");
        AppRepositoryArtifact art = new AppRepositoryArtifact(artifact,repository);
        //then
        assertThrows(DependencyResolverException.class, () -> MetadataLoader.loadMetadata(art));

    }

    private static WireMockServer mockMetaData(String content) {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort());
        wireMockServer.stubFor(get("/repository/pacos-maven-repo/org/pacos/info/module/maven-metadata.xml")
                .willReturn(aResponse().withBody(content).withStatus(200)));
        wireMockServer.start();
        return wireMockServer;
    }

    private static String getValidResponse() throws IOException {
        return new String(Objects.requireNonNull(MetadataLoaderTest.class.getClassLoader()
                .getResourceAsStream("test_maven_metadata.xml")).readAllBytes());
    }

}
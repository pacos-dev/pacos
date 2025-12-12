package org.pacos.config.repository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import org.pacos.config.property.ApplicationProperties;
import org.pacos.config.property.PropertyName;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.repository.data.AppRepositoryArtifact;
import org.pacos.config.repository.data.ModuleConfiguration;
import org.pacos.config.repository.info.Plugin;
import org.pacos.config.repository.info.PluginInfo;
import org.pacos.config.repository.metadata.Metadata;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepositoryClientTest {


    @Test
    void whenLoadModuleListThenLoadLatestModuleVersionConfiguration() throws IOException, URISyntaxException {
        //given
        WireMockServer server = prepareServer();
        mockMetaData(server);
        mockModuleList(server);
        //when
        System.setProperty(PropertyName.MODULE_LIST_REPO_URL.getPropertyName(),
                "http://localhost:" + server.port() + "/repository/pacos-maven-repo");
        ApplicationProperties.reloadProperties();

        ModuleConfiguration listDTO = RepositoryClient.loadModule();
        //then
        assertEquals("1.0.0", listDTO.version());
    }


    @Test
    void whenIncorrectModuleJsonFileThenThrowException() throws IOException, URISyntaxException {
        //given
        WireMockServer server = prepareServer();
        mockMetaData(server);
        server.stubFor(get("/repository/pacos-maven-repo/org/pacos/info/module/1.0.0/module-1.0.0.json")
                .willReturn(aResponse().withBody("<json></json>").withStatus(200)));
        //when
        System.setProperty(PropertyName.MODULE_LIST_REPO_URL.getPropertyName(),
                "http://localhost:" + server.port() + "/repository/pacos-maven-repo");
        ApplicationProperties.reloadProperties();

        assertThrows(DependencyResolverException.class, RepositoryClient::loadModule);
    }

    @Test
    void whenCantFetchLatestVersionFromManifestThenThrowException() throws IOException, URISyntaxException {
        //given
        WireMockServer server = prepareServer();
        mockMetaData(server);
        //when
        System.setProperty(PropertyName.MODULE_LIST_REPO_URL.getPropertyName(),
                "http://localhost:" + server.port() + "/repository/pacos-maven-repo");
        ApplicationProperties.reloadProperties();

        assertThrows(DependencyResolverException.class, RepositoryClient::loadModule);
    }

    @Test
    void whenGetPluginListThenReturnPluginList() throws IOException, URISyntaxException {
        //given
        WireMockServer server = prepareServer();
        mockPluginMetaData(server);
        mockPluginData(server);
        mockPluginVersion(server, "database");
        mockPluginVersion(server, "explorer");
        mockPluginVersion(server, "glogg");
        mockPluginVersion(server, "apinity");
        mockPluginVersion(server, "mockserver");


        System.setProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName(), "http://localhost:" + server.port() + "/repository/pacos-maven-repo");
        ApplicationProperties.reloadProperties();
        //when
        List<Plugin> plugins = RepositoryClient.loadPluginList(AppRepository.pluginRepo());
        //then
        assertEquals(5, plugins.size());
    }

    @Test
    void whenGetPluginListThenDatabaseIsNotListedBecauseHasNoVersion() throws IOException, URISyntaxException {
        //given
        WireMockServer server = prepareServer();
        mockPluginMetaData(server);
        mockPluginData(server);

        mockPluginVersion(server, "explorer");
        mockPluginVersion(server, "glogg");
        mockPluginVersion(server, "apinity");
        mockPluginVersion(server, "mockserver");


        System.setProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName(), "http://localhost:" + server.port() + "/repository/pacos-maven-repo");
        ApplicationProperties.reloadProperties();
        //when
        List<Plugin> listDTO = RepositoryClient.loadPluginList(AppRepository.pluginRepo());
        //then
        Optional<Plugin> database = listDTO.stream().filter(m -> m.artifactName().equals("database")).findFirst();
        assertFalse(database.isPresent());
    }

    @Test
    void whenLoadModuleInfoThenReturnValidObject() throws IOException, URISyntaxException {
        WireMockServer server = prepareServer();
        mockPluginInfo(server);
        System.setProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName(), "http://localhost:" + server.port() + "/repository/pacos-maven-repo");
        ApplicationProperties.reloadProperties();

        AppArtifact artifact = new AppArtifact("org.pacos", "plugin", "1.0.0");
        AppRepository repository = AppRepository.pluginRepo();
        AppRepositoryArtifact repositoryArtifact = new AppRepositoryArtifact(artifact, repository);

        //when
        PluginInfo pluginInfo = RepositoryClient.loadInfo(repositoryArtifact);
        //then
        assertNotNull(pluginInfo);
        assertNotNull(pluginInfo.releases());
        assertEquals("Test plugin", pluginInfo.name());
        assertEquals("PacOS Team", pluginInfo.author());
        assertEquals(" Introducing ", pluginInfo.description());
        assertEquals(2, pluginInfo.releases().size());
        assertEquals("1.0.0 (2023-10-21)", pluginInfo.releases().get(0).version());
        assertEquals("Font manipulation", pluginInfo.releases().get(0).changes().get(0));
    }

    @Test
    void whenCantLoadModuleInfoThenThrowException() {
        WireMockServer server = prepareServer();
        System.setProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName(), "http://localhost:" + server.port() + "/repository/pacos-maven-repo");
        ApplicationProperties.reloadProperties();

        AppArtifact artifact = new AppArtifact("org.pacos", "plugin", "1.0.0");
        AppRepository repository = AppRepository.pluginRepo();
        AppRepositoryArtifact repositoryArtifact = new AppRepositoryArtifact(artifact, repository);

        //when
        assertThrows(DependencyResolverException.class, () -> RepositoryClient.loadInfo(repositoryArtifact));
    }

    @Test
    void whenLoadMetadataThenReturnValidObject() throws IOException, URISyntaxException {
        WireMockServer server = prepareServer();
        mockPluginMetaData(server);

        System.setProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName(), "http://localhost:" + server.port() + "/repository/pacos-maven-repo");
        ApplicationProperties.reloadProperties();
        //when
        AppArtifact artifact = new AppArtifact("org.pacos.info", "plugin", null);
        AppRepository repository = AppRepository.pluginRepo();
        AppRepositoryArtifact repositoryArtifact = new AppRepositoryArtifact(artifact, repository);
        Metadata metadata = RepositoryClient.loadMetadata(repositoryArtifact);
        //then
        assertNotNull(metadata);
    }

    private static WireMockServer prepareServer() {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort());
        wireMockServer.start();
        return wireMockServer;
    }

    private static void mockModuleList(WireMockServer wireMockServer) throws IOException {

        String moduleJson = new String(Objects.requireNonNull(RepositoryClientTest.class.getClassLoader()
                .getResourceAsStream("test_module_list.json")).readAllBytes());
        wireMockServer.stubFor(get("/repository/pacos-maven-repo/org/pacos/info/module/1.0.0/module-1.0.0.json")
                .willReturn(aResponse().withBody(moduleJson).withStatus(200)));
    }

    private static void mockMetaData(WireMockServer wireMockServer) throws IOException, URISyntaxException {
        URL resourceUrl = RepositoryClientTest.class.getClassLoader().getResource("test_maven_metadata.xml");
        Objects.requireNonNull(resourceUrl);
        String metadataXml = new String(Files.readAllBytes(Paths.get(resourceUrl.toURI())));


        wireMockServer.stubFor(get("/repository/pacos-maven-repo/org/pacos/info/module/maven-metadata.xml")
                .willReturn(aResponse().withBody(metadataXml).withStatus(200)));
    }

    private static void mockPluginMetaData(WireMockServer wireMockServer) throws IOException, URISyntaxException {
        URL resourceUrl = RepositoryClientTest.class.getClassLoader().getResource("plugin/test_maven_metadata_plugin_1.0.0.xml");
        Objects.requireNonNull(resourceUrl);
        String metadataXml = new String(Files.readAllBytes(Paths.get(resourceUrl.toURI())));

        wireMockServer.stubFor(get("/repository/pacos-maven-repo/org/pacos/info/plugin/maven-metadata.xml")
                .willReturn(aResponse().withBody(metadataXml).withStatus(200)));
    }

    private void mockPluginVersion(WireMockServer server, String pluginName) throws URISyntaxException, IOException {
        URL resourceUrl = RepositoryClientTest.class.getClassLoader().getResource("plugin/test_maven_metadata_plugin_1.0.0.xml");
        Objects.requireNonNull(resourceUrl);
        String metadataXml = new String(Files.readAllBytes(Paths.get(resourceUrl.toURI())));

        server.stubFor(get("/repository/pacos-maven-repo/org/pacos/plugin/" + pluginName + "/maven-metadata.xml")
                .willReturn(aResponse().withBody(metadataXml).withStatus(200)));
    }

    private static void mockPluginData(WireMockServer wireMockServer) throws IOException, URISyntaxException {
        URL resourceUrl = RepositoryClientTest.class.getClassLoader().getResource("plugin/test_plugin_list_1.0.0.json");
        Objects.requireNonNull(resourceUrl);
        String moduleJson = new String(Files.readAllBytes(Paths.get(resourceUrl.toURI())));

        wireMockServer.stubFor(get("/repository/pacos-maven-repo/org/pacos/info/plugin/1.0.0/plugin-1.0.0.json")
                .willReturn(aResponse().withBody(moduleJson).withStatus(200)));
    }

    private static void mockPluginInfo(WireMockServer wireMockServer) throws IOException, URISyntaxException {

        URL resourceUrl = RepositoryClientTest.class.getClassLoader().getResource("plugin/plugin_info.xml");
        Objects.requireNonNull(resourceUrl);
        String moduleJson = new String(Files.readAllBytes(Paths.get(resourceUrl.toURI())));


        wireMockServer.stubFor(get("/repository/pacos-maven-repo/org/pacos/plugin/1.0.0/plugin-1.0.0.xml")
                .willReturn(aResponse().withBody(moduleJson).withStatus(200)));
    }
}
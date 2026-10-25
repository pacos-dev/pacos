package org.pacos.e2e;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarOutputStream;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

/**
 * A deterministic Maven-layout repository used by browser E2E tests.
 *
 * It serves the same metadata -> plugin catalog -> artifact metadata -> JAR flow
 * used by the production repository client and Maven Resolver. Nothing in this
 * test depends on the live PacOS repository.
 */
final class MockPluginRepository implements AutoCloseable {
    private static final String REPOSITORY_PATH = "/repository/pacos-maven-repo";
    private static final String GROUP_PATH = "/org/pacos/plugin/skeleton";
    private static final String VERSION = "3.1";
    private static final String CATALOG_RESOURCE = "/mock-plugin-repository/plugin-catalog.json";
    private static final String POM_RESOURCE = "/mock-plugin-repository/skeleton-3.1.pom";
    private static final String PLUGIN_METADATA_RESOURCE = "/mock-plugin-repository/skeleton-3.1.xml";

    private final WireMockServer server = new WireMockServer(
            WireMockConfiguration.wireMockConfig().dynamicPort());

    MockPluginRepository() {
        server.start();
        stubText("/org/pacos/info/plugin/maven-metadata.xml", metadata("org.pacos.info", "plugin", "1.0.0"));
        stubResource("/org/pacos/info/plugin/1.0.0/plugin-1.0.0.json", CATALOG_RESOURCE,
                "application/json; charset=utf-8", true);
        stubText(GROUP_PATH + "/maven-metadata.xml", metadata("org.pacos.plugin", "skeleton", VERSION));
        stubPng(GROUP_PATH + "/" + VERSION + "/skeleton-" + VERSION + ".png");
        stubResource(GROUP_PATH + "/" + VERSION + "/skeleton-" + VERSION + ".pom", POM_RESOURCE,
                "application/xml; charset=utf-8", false);
        stubResource(GROUP_PATH + "/" + VERSION + "/skeleton-" + VERSION + ".xml", PLUGIN_METADATA_RESOURCE,
                "application/xml; charset=utf-8", false);
        stubJarFromClasspath();
    }

    String url() {
        return "http://127.0.0.1:" + server.port() + REPOSITORY_PATH;
    }

    @Override
    public void close() {
        server.stop();
    }

    private void stubPng(String path) {
        byte[] png = java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jV2cAAAAASUVORK5CYII=");
        server.stubFor(get(REPOSITORY_PATH + path)
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "image/png")
                        .withBody(png)));
    }

    private void stubText(String path, String body) {
        server.stubFor(get(REPOSITORY_PATH + path)
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/xml; charset=utf-8")
                        .withBody(body)));
    }

    private void stubResource(String path, String resource, String contentType, boolean injectRepositoryUrl) {
        String body = readResource(resource);
        if (injectRepositoryUrl) {
            body = body.replace("${repositoryUrl}", url());
        }
        server.stubFor(get(REPOSITORY_PATH + path)
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", contentType)
                        .withBody(body)));
    }

    private void stubJarFromClasspath() {
        try (InputStream input = MockPluginRepository.class.getResourceAsStream("/skeleton.jar")) {
            if (input == null) {
                throw new IllegalStateException(
                        "Required real E2E plugin JAR is missing from e2e-tests/src/test/resources/skeleton.jar");
            }
            byte[] jar = input.readAllBytes();
            server.stubFor(get(REPOSITORY_PATH + GROUP_PATH + "/" + VERSION + "/skeleton-" + VERSION + ".jar")
                    .willReturn(aResponse().withStatus(200)
                            .withHeader("Content-Type", "application/java-archive")
                            .withBody(jar)));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read the E2E skeleton plugin JAR", exception);
        }
    }

    private static String readResource(String resource) {
        try (InputStream input = MockPluginRepository.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Required E2E repository fixture is missing: " + resource);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read E2E repository fixture: " + resource, exception);
        }
    }

    private static String metadata(String groupId, String artifactId, String version) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <metadata>
                  <groupId>%s</groupId>
                  <artifactId>%s</artifactId>
                  <versioning>
                    <latest>%s</latest>
                    <release>%s</release>
                    <versions><version>%s</version></versions>
                    <lastUpdated>20261025193000</lastUpdated>
                  </versioning>
                </metadata>
                """.formatted(groupId, artifactId, version, version, version);
    }
}

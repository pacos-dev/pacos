package org.pacos.e2e;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    private final WireMockServer server = new WireMockServer(
            WireMockConfiguration.wireMockConfig().dynamicPort());

    MockPluginRepository() {
        server.start();
        stubText("/org/pacos/info/plugin/maven-metadata.xml", metadata("org.pacos.info", "plugin", "1.0.0"));
        stubText("/org/pacos/info/plugin/1.0.0/plugin-1.0.0.json", """
                {
                  "plugins": [{
                    "groupId": "org.pacos.plugin",
                    "artifactName": "skeleton",
                    "name": "Skeleton app",
                    "author": "PacOS",
                    "icon": "%s/org/pacos/plugin/skeleton/3.1/skeleton-3.1.png"
                  }]
                }
                """.formatted(url()));
        stubText(GROUP_PATH + "/maven-metadata.xml", metadata("org.pacos.plugin", "skeleton", VERSION));
        stubPng(GROUP_PATH + "/" + VERSION + "/skeleton-" + VERSION + ".png");
        stubText(GROUP_PATH + "/" + VERSION + "/skeleton-" + VERSION + ".pom", """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>org.pacos.plugin</groupId>
                  <artifactId>skeleton</artifactId>
                  <version>3.1</version>
                  <packaging>jar</packaging>
                </project>
                """);
        stubText(GROUP_PATH + "/" + VERSION + "/skeleton-" + VERSION + ".xml", """
                <plugin>
                  <name>Skeleton app</name>
                  <author>Your organization</author>
                  <description>Skeleton plugin served by the isolated PacOS E2E repository.</description>
                  <releases>
                    <release>
                      <version>3.1</version>
                      <changes><change>Test repository artifact</change></changes>
                    </release>
                  </releases>
                </plugin>
                """);
        stubJarFromBuildArtifact();
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
        String contentType = path.endsWith(".json") ? "application/json; charset=utf-8" : "application/xml; charset=utf-8";
        server.stubFor(get(REPOSITORY_PATH + path)
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", contentType)
                        .withBody(body)));
    }

    private void stubJarFromBuildArtifact() {
        Path[] candidates = {
                Path.of("e2e-tests", "src", "test", "resources", "skeleton.jar"),
                Path.of("e2e-tests", "src", "test", "resources", "test-plugins", "skeleton-3.1.jar"),
                Path.of("/mnt/data", "skeleton-3.1.jar")
        };
        for (Path candidate : candidates) {
            try {
                if (Files.isRegularFile(candidate)) {
                    stubJar(Files.readAllBytes(candidate));
                    return;
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to read the E2E skeleton plugin JAR", exception);
            }
        }

        throw new IllegalStateException(
                "Required real E2E plugin JAR is missing. Expected e2e-tests/src/test/resources/skeleton.jar");

    }

    private void stubJar(byte[] jar) {
        server.stubFor(get(REPOSITORY_PATH + GROUP_PATH + "/" + VERSION + "/skeleton-" + VERSION + ".jar")
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/java-archive")
                        .withBody(jar)));
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

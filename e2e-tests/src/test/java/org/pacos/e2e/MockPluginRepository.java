package org.pacos.e2e;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import javax.tools.ToolProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;

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

        // Jenkins does not receive conversation attachments. Keep CI self-contained by
        // generating a minimal valid plugin archive if the supplied skeleton JAR is not
        // checked into test resources. It exercises the same Maven download and classloader
        // lifecycle without depending on the live repository.
        try {
            stubJar(createMinimalPluginJar());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create the fallback E2E plugin JAR", exception);
        }
    }

    private void stubJar(byte[] jar) {
        server.stubFor(get(REPOSITORY_PATH + GROUP_PATH + "/" + VERSION + "/skeleton-" + VERSION + ".jar")
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/java-archive")
                        .withBody(jar)));
    }

    private static byte[] createMinimalPluginJar() throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("E2E plugin fallback requires a JDK compiler");
        }
        Path work = Files.createTempDirectory("pacos-e2e-plugin-");
        Path configSource = work.resolve("src/org/pacos/plugin/skeleton/config/SkeletonWindowConfig.java");
        Path windowSource = work.resolve("src/org/pacos/plugin/skeleton/config/SkeletonWindow.java");
        Path classes = work.resolve("classes");
        Files.createDirectories(configSource.getParent());
        Files.createDirectories(classes);
        Files.writeString(configSource, """
                package org.pacos.plugin.skeleton.config;
                @org.springframework.stereotype.Component
                public class SkeletonWindowConfig implements org.pacos.base.window.config.WindowConfig {
                    public String title() { return "ToDo list"; }
                    public String icon() { return "e2e-icon.png"; }
                    public Class<? extends org.pacos.base.window.DesktopWindow> activatorClass() { return SkeletonWindow.class; }
                    public boolean isApplication() { return true; }
                    public boolean isAllowMultipleInstance() { return true; }
                }
                """, StandardCharsets.UTF_8);
        Files.writeString(windowSource, """
                package org.pacos.plugin.skeleton.config;
                @org.springframework.stereotype.Component
                @org.springframework.context.annotation.Scope("prototype")
                public class SkeletonWindow extends org.pacos.base.window.DesktopWindow {
                    public SkeletonWindow(SkeletonWindowConfig config) {
                        super(config);
                        add(new com.vaadin.flow.component.html.Span("Skeleton plugin E2E window"));
                    }
                }
                """, StandardCharsets.UTF_8);
        int result = compiler.run(null, null, null,
                "-classpath", compilerClasspath(),
                "-d", classes.toString(), configSource.toString(), windowSource.toString());
        if (result != 0) {
            throw new IOException("Unable to compile fallback plugin configuration; javac exit code " + result);
        }
        Path configClassFile = classes.resolve("org/pacos/plugin/skeleton/config/SkeletonWindowConfig.class");
        Path windowClassFile = classes.resolve("org/pacos/plugin/skeleton/config/SkeletonWindow.class");
        try (var bytes = new java.io.ByteArrayOutputStream();
             var jar = new JarOutputStream(bytes)) {
            jar.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            jar.write(("Manifest-Version: 1.0\r\n"
                    + "Implementation-Title: Skeleton app\r\n"
                    + "Implementation-Group: org.pacos.plugin\r\n"
                    + "Implementation-Version: 3.1\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            jar.closeEntry();
            jar.putNextEntry(new JarEntry("org/pacos/plugin/skeleton/config/SkeletonWindowConfig.class"));
            jar.write(Files.readAllBytes(configClassFile));
            jar.closeEntry();
            jar.putNextEntry(new JarEntry("org/pacos/plugin/skeleton/config/SkeletonWindow.class"));
            jar.write(Files.readAllBytes(windowClassFile));
            jar.closeEntry();
            jar.putNextEntry(new JarEntry("META-INF/resources/e2e-icon.png"));
            jar.write(java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jV2cAAAAASUVORK5CYII="));
            jar.closeEntry();
            return bytes.toByteArray();
        } finally {
            try (var paths = Files.walk(work)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (IOException ignored) { /* temp cleanup */ }
                });
            }
        }
    }

    private static String compilerClasspath() {
        Set<String> entries = new LinkedHashSet<>();
        String systemClasspath = System.getProperty("java.class.path", "");
        if (!systemClasspath.isBlank()) {
            entries.addAll(java.util.Arrays.asList(systemClasspath.split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))));
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        while (loader != null) {
            if (loader instanceof URLClassLoader urlClassLoader) {
                for (URL url : urlClassLoader.getURLs()) {
                    try {
                        if ("file".equals(url.getProtocol())) {
                            entries.add(Path.of(url.toURI()).toString());
                        }
                    } catch (Exception ignored) {
                        // Ignore non-file classpath entries; the compiler will report missing types.
                    }
                }
            }
            loader = loader.getParent();
        }
        return String.join(java.io.File.pathSeparator, entries);
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

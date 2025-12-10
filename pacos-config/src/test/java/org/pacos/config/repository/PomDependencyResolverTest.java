package org.pacos.config.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class PomDependencyResolverTest {

    @TempDir
    private Path tempDir;

    private final String contentPom = """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
            
                <groupId>org.test</groupId>
                <artifactId>test-artifact</artifactId>
                <version>1.0.1</version>
            
                <properties>
                    <maven.resolver.version>17</maven.resolver.version>
                </properties>
                <dependencies>
                    <dependency>
                        <groupId>org.apache.maven.resolver</groupId>
                        <artifactId>maven-resolver-impl</artifactId>
                        <version>${maven.resolver.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.maven.resolver</groupId>
                        <artifactId>maven-resolver-connector-basic</artifactId>
                        <version>1.9.18</version>
                    </dependency>
                    <dependency>
                        <groupId>org.test</groupId>
                        <artifactId>test</artifactId>
                        <version>1.11</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.myArt</groupId>
                        <artifactId>test2</artifactId>
                        <version>1.0</version>
                        <scope>provided</scope>
                    </dependency>
                </dependencies>
            </project>
            """;
    private Path pomFile;

    @BeforeEach
    void init() throws IOException {
        if (!tempDir.resolve("lib").toFile().mkdirs()) {
            fail();
        }
        System.setProperty("workingDir", tempDir.toString());
        this.pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, contentPom);
    }


    @Test
    void whenCantResolveSingleArtifactThenReturnFalse() {
        AppRepository testRepo = new AppRepository("123", "http://localhost:8012");
        AppArtifact artifact = new AppArtifact("org.info", "artifact", "1.0.0");
        assertFalse(PomDependencyResolver.resolve(artifact, testRepo, "jar"));
    }

    @Test
    void whenResolveEmptyModuleListThenReturnEmptyModuleList() {
        //then
        assertDoesNotThrow(() -> PomDependencyResolver.resolve(new ArrayList<>()));
    }

    @Test
    void whenCantResolveModuleListThenThrowException() {
        AppArtifact module = new AppArtifact("group", "artifact", "1.0");
        var modules = List.of(module);
        //when

        DependencyResolverException exception = assertThrows(DependencyResolverException.class,
                () -> PomDependencyResolver.resolve(modules));
        //then
        assertEquals("Can't download dependency artifact-1.0.jar", exception.getMessage());
    }

}
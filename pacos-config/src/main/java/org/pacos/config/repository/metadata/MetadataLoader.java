package org.pacos.config.repository.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.pacos.config.repository.DependencyResolverException;
import org.pacos.config.repository.data.AppRepositoryArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MetadataLoader {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataLoader.class);

    private MetadataLoader() {
    }

    public static Metadata loadMetadata(AppRepositoryArtifact repositoryArtifact) {
        String url = repositoryArtifact.getMetaDataUrl();
        LOG.info("Calling url: {}", url);
        try (InputStream inputStream = new URI(url).toURL().openStream()) {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(inputStream);

            NodeList versionElements = document.getElementsByTagName("version");
            Versions versions = new Versions(new ArrayList<>());
            for (int i = 0; i < versionElements.getLength(); i++) {
                Element versionElement = (Element) versionElements.item(i);
                String version = versionElement.getTextContent();
                versions.version().add(version);
            }

            NodeList groupIdElement = document.getElementsByTagName("groupId");
            String groupId = groupIdElement.getLength() > 0 ? groupIdElement.item(0).getTextContent() : null;

            NodeList artifactIdElement = document.getElementsByTagName("artifactId");
            String artifactId = artifactIdElement.getLength() > 0 ? artifactIdElement.item(0).getTextContent() : null;
            NodeList latestElement = document.getElementsByTagName("latest");
            String latest = latestElement.getLength() > 0 ? latestElement.item(0).getTextContent() : null;
            LOG.info("Find latest version: {}", latest);
            NodeList releaseElement = document.getElementsByTagName("release");
            String release = releaseElement.getLength() > 0 ? releaseElement.item(0).getTextContent() : null;

            NodeList lastUpdatedElement = document.getElementsByTagName("lastUpdated");
            String lastUpdated = lastUpdatedElement.getLength() > 0 ? lastUpdatedElement.item(0).getTextContent() : null;
            Versioning versioning = new Versioning(latest, release, versions, lastUpdated);

            return new Metadata(groupId, artifactId, versioning);

        } catch (ParserConfigurationException | SAXException | IOException | URISyntaxException e) {
            LOG.error("Can't load metadata for {}", repositoryArtifact.getMetaDataUrl());
           throw new DependencyResolverException("Can't load maven-metadata.xml for "+repositoryArtifact,e);
        }
    }

}

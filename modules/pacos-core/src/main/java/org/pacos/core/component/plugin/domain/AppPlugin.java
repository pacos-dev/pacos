package org.pacos.core.component.plugin.domain;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;

/**
 * Information about plugin installed in the pacos.
 * Contains many dependencies (based on plugin POM file)
 */
@Entity
@Table(name = "app_plugin")
public class AppPlugin {

    @Id
    private String id;
    @Column(name = "group_id")
    private String groupId;
    @Column(name = "artifact_name")
    private String artifactName;
    @Column(name = "version")
    private String version;
    @Column(name = "removed")
    private boolean removed;
    @Column(name = "disabled")
    private boolean disabled;
    @Column(name = "repo_url")
    private String repoUrl;
    @Getter
    @Column(name = "name")
    private String name;
    @Getter
    @Column(name = "author")
    private String author;
    @Getter
    @Column(name = "icon")
    private String icon;

    public AppPlugin() {
    }

    public AppPlugin(String groupId, String artifactName, String version) {
        this.groupId = groupId;
        this.artifactName = artifactName;
        this.version = version;
        this.id = getId();
    }

    public String getGroupId() {
        return groupId;
    }


    public String getArtifactName() {
        return artifactName;
    }

    public String getVersion() {
        return version;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isRemoved() {
        return removed;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppPlugin pacosPlugin = (AppPlugin) o;
        return Objects.equals(id, pacosPlugin.id);
    }

    @Override
    public String toString() {
        return "PacosPlugin{" +
                "groupId='" + groupId + '\'' +
                ", artifactName='" + artifactName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

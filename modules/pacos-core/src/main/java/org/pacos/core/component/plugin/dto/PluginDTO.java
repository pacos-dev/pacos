package org.pacos.core.component.plugin.dto;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;

import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.info.Plugin;
import org.pacos.config.repository.info.PluginInfo;

public class PluginDTO implements Serializable {

    private String groupId;
    private String artifactName;
    private String version;
    private String repoUrl;
    private String name;
    private String author;
    private String icon;
    private boolean removed;
    private boolean disabled;
    private transient PluginInfo pluginInfoDTO;
    //App value
    private String errMsg;

    public PluginDTO() {
    }

    public PluginDTO(Plugin plugin) {
        this.groupId = plugin.groupId();
        this.version = plugin.version();
        this.artifactName = plugin.artifactName();
        this.author = plugin.author();
        this.name = plugin.name();
        this.icon = plugin.icon();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public PluginInfo getPluginInfoDTO() {
        return pluginInfoDTO;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public void setPluginInfoDTO(PluginInfo pluginInfoDTO) {
        this.pluginInfoDTO = pluginInfoDTO;
    }


    public boolean equalsWithoutVersion(PluginDTO other) {
        return getArtifactName().equals(other.getArtifactName()) &&
                getGroupId().equals(other.getGroupId());
    }

    public Path getIconAbsolutePath() {
        return WorkingDir.getModulePath(getArtifactName()).resolve("icon")
                .resolve(getArtifactName() + ".png");
    }

    @Override
    public String toString() {
        return "PluginDTO{" +
                "name='" + name + '\'' +
                ", removed=" + removed +
                ", disabled=" + disabled +
                ", errMsg='" + errMsg + "'} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginDTO pluginDTO = (PluginDTO) o;
        return Objects.equals(getGroupId(), pluginDTO.getGroupId()) && Objects.equals(getArtifactName(), pluginDTO.getArtifactName()) && Objects.equals(getVersion(), pluginDTO.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupId(), getArtifactName(), getVersion());
    }

    public AppArtifact toArtifact() {
        return new AppArtifact(getGroupId(), getArtifactName(), getVersion());
    }
}

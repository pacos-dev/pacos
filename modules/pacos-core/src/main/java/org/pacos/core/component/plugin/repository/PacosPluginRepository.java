package org.pacos.core.component.plugin.repository;

import java.util.List;
import java.util.Optional;

import org.pacos.core.component.plugin.domain.AppPlugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PacosPluginRepository extends JpaRepository<AppPlugin, String> {
    Optional<AppPlugin> findByGroupIdAndArtifactNameAndVersion(String groupId, String artifactName,String version);

    Optional<AppPlugin> findByGroupIdAndArtifactNameAndDisabled(String groupId, String artifactName, boolean disabled);
    List<AppPlugin> findByArtifactNameAndGroupId(String artifactName, String groupId);

    @Modifying(clearAutomatically = true)
    @Query("update AppPlugin p SET p.disabled=true WHERE p.version not like :majorVersion")
    void disableAllPluginsWhereVersionIsNotLike(@Param("majorVersion") String majorVersion);

    List<AppPlugin> findAllByDisabled(boolean disabled);

    List<AppPlugin> findAllByRemoved(boolean removed);
}

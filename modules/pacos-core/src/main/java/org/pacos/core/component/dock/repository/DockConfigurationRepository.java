package org.pacos.core.component.dock.repository;

import java.util.List;

import org.pacos.core.component.dock.domain.DockConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DockConfigurationRepository extends JpaRepository<DockConfiguration, Integer> {

    List<DockConfiguration> findByUserIdOrderByOrderNum(Integer userId);

    @Modifying
    @Query("DELETE FROM DockConfiguration WHERE activatorClass=:activator AND userId=:userId")
    void removeActivator(@Param("activator") String activatorClass, @Param("userId") int userId);
}

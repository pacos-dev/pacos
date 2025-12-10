package org.pacos.core.component.dock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Handles information about dock configuration for each user
 * Single entry represents single icon on dock
 */
@Entity
@Table(name = "app_dock_config",
        uniqueConstraints =
        @UniqueConstraint(columnNames = { "user_id", "activator" }))
@Setter
@Getter
@SequenceGenerator(
        name = "dock-seq-gen",
        sequenceName = "APP_DOCK_CONFIG_SEQ",
        allocationSize = 1)
@EqualsAndHashCode(of = {"id"})
public class DockConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dock-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "order_num", nullable = false)
    private int orderNum;

    @Column(name = "activator")
    private String activatorClass;

    @Column(name = "user_id")
    private Integer userId;

    public DockConfiguration() {
    }

    public DockConfiguration(String activatorClassName, int userId) {
        this.userId = userId;
        this.activatorClass = activatorClassName;
    }
}

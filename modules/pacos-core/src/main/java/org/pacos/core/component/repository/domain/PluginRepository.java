package org.pacos.core.component.repository.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP_PLUGIN_REPOSITORY")
@Getter
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "id")
public class PluginRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "REPOSITORY_NAME", nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "REPOSITORY_URL", nullable = false, unique = true, length = 2048)
    private String url;

    protected PluginRepository() {
    }

    public PluginRepository(String name, String url) {
        this.name = name;
        this.url = url;
    }
}

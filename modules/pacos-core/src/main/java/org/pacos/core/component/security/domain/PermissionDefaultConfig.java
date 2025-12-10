package org.pacos.core.component.security.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import org.pacos.base.session.AccessDecision;

@Entity
@Table(name = "APP_PERMISSION_DEF")
@Getter
@Setter
public class PermissionDefaultConfig {

    @Id
    @Column(name = "action_key")
    private String key;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "action_key")
    private Permissions action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccessDecision decision;

    public PermissionDefaultConfig() {
        //empty for jpa
    }

    public PermissionDefaultConfig(Permissions e) {
        action = e;
        decision = AccessDecision.DENY;
    }
}

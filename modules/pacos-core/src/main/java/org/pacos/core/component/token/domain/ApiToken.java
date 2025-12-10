package org.pacos.core.component.token.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.pacos.core.config.database.AbstractEntity;

/**
 * Information about plugin installed in the pacos.
 * Contains many dependencies (based on plugin POM file)
 */
@Entity
@Table(name = "app_api_token")
@Getter(AccessLevel.PUBLIC)
@EqualsAndHashCode(of = "name", callSuper = false)
public class ApiToken extends AbstractEntity {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "token")
    private String token;

    @Column(name = "expired_on")
    private LocalDate expiredOn;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApiTokenStatus status;

    public ApiToken() {
    }

    public ApiToken(String name, String token, LocalDate expiredOn) {
        this.name = name;
        this.token = token;
        this.expiredOn = expiredOn;
        this.status = ApiTokenStatus.ACTIVE;
    }

}

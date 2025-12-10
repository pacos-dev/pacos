package org.pacos.core.component.security.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;
import org.pacos.base.session.AccessDecision;
import org.pacos.core.component.user.domain.User;

@Entity
@Table(name = "APP_PERMISSION_USER",
        uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "action_key" }))
@SequenceGenerator(
        name = "action-user-seq-gen",
        sequenceName = "APP_PERMISSION_USER_SEQ",
        allocationSize = 1)
@Getter
@Setter
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "action-user-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "action_key",nullable = false)
    private Permissions action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessDecision decision;

    public UserPermission() {
    }
}
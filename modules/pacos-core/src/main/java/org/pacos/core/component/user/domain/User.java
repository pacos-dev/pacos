package org.pacos.core.component.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_user")
@Setter
@Getter
@SequenceGenerator(
        name = "user-seq-gen",
        sequenceName = "APP_USER_SEQ",
        allocationSize = 1)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "login")
    private String login;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "token")
    private String token;

    @Column(name = "variable_collection_id")
    private Integer variableCollectionId;

}

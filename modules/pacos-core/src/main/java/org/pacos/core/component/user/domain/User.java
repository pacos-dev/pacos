package org.pacos.core.component.user.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pacos.core.component.security.domain.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "APP_ROLE_USER",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public List<String> getRoleList() {
        return roles.stream().map(Role::getLabel).toList();
    }
}

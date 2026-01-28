package org.pacos.core.component.security.domain;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.Length;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP_ROLE")
@Getter
@Setter
@SequenceGenerator(
        name = "role-seq-gen",
        sequenceName = "APP_ROLE_SEQ",
        initialValue = 3,
        allocationSize = 1)
@EqualsAndHashCode(of = { "label" })
public class Role {

    public static final Integer ROOT_ROLE = 2;
    public static final Integer GUEST_ROLE = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "label", unique = true)
    @Length(max = 255)
    private String label;

    @Length(max = 2000)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "APP_ROLE_PERMISSION",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<AppPermission> appPermissions = new HashSet<>();

    public Role() {
        //empty for jpa
    }
}
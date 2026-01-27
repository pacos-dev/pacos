package org.pacos.core.component.security.domain;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "APP_PERMISSION")
@Getter
@Setter
@SequenceGenerator(
        name = "permission-seq-gen",
        sequenceName = "APP_PERMISSION_SEQ",
        allocationSize = 1)
@EqualsAndHashCode(of = { "key" })
public class AppPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permission-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "key", unique = true)
    private String key;

    @Length(max = 255)
    private String label;
    @Length(max = 255)
    private String category;
    @Length(max = 2000)
    private String description;

    public AppPermission() {
        //empty for jpa
    }

    public AppPermission(org.pacos.base.security.Permission e) {
        key = e.getKey();
        label = e.getLabel();
        category = e.getCategory();
        description = e.getDescription();
    }

}
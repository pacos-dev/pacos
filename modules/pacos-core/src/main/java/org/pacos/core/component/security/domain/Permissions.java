package org.pacos.core.component.security.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.pacos.base.security.Permission;

@Entity
@Table(name = "APP_PERMISSION")
@Getter
@Setter
@EqualsAndHashCode(of = { "key" })
public class Permissions {

    @Id
    @Column(name = "key")
    private String key;

    @Length(max = 255)
    private String label;
    @Length(max = 255)
    private String category;
    @Length(max = 2000)
    private String description;

    public Permissions() {
        //empty for jpa
    }

    public Permissions(Permission e) {
        key = e.getKey();
        label = e.getLabel();
        category = e.getCategory();
        description = e.getDescription();
    }
}
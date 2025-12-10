package org.pacos.core.component.variable.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_system_variable")
@Setter
@Getter
@SequenceGenerator(
        name = "system-variable-seq-gen",
        sequenceName = "SYSTEM_VARIABLE_SEQ",
        allocationSize = 1)
public class SystemVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system-variable-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "js", length = 1000000)
    @Lob
    private String js;
}

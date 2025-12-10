package org.pacos.core.component.variable.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "app_user_variable",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "collection_id"}
        ))
@Setter
@Getter
@SequenceGenerator(
        name = "user-variable-seq-gen",
        sequenceName = "USER_VARIABLE_SEQ",
        allocationSize = 1)
public class UserVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user-variable-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;
    @Column(name = "enabled")
    private boolean enabled;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "collection_id")
    private Integer collectionId;
    @Column(name = "initial_value")
    private String initialValue;
    @Column(name = "current_value")
    private String currentValue;
    @ManyToOne
    @JoinColumn(name = "collection_id", updatable = false, insertable = false)
    private UserVariableCollection collection;

}

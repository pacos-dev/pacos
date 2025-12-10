package org.pacos.core.component.variable.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_user_variable_collection")
@Setter
@Getter
@SequenceGenerator(
        name = "user-variable-col-seq-gen",
        sequenceName = "USER_VARIABLE_COL_SEQ",
        allocationSize = 1)
public class UserVariableCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user-variable-col-seq-gen")
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "global")
    private boolean global;

    @OneToMany(mappedBy = "collection", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<UserVariable> variables = new ArrayList<>();
}

package org.pacos.core.component.variable.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.pacos.common.data.HasName;

@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class UserVariableCollectionDTO implements HasName, Serializable {

    public static final String GLOBAL_NAME = "Global";
    @Getter
    @Setter
    private Integer id;
    @Getter
    @Setter
    private String name;
    @Getter
    private int userId;
    @Getter
    private boolean global;
    @Setter
    @Getter
    private List<UserVariableDTO> variables;

    public String getDisplayName() {
        return name;
    }
}

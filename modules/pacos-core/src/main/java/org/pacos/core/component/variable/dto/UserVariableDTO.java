package org.pacos.core.component.variable.dto;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.pacos.common.view.param.Param;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class UserVariableDTO {
    @Getter
    @Setter
    private Integer collectionId;
    @Getter
    @Setter
    protected Integer id;
    @Getter
    @Setter
    protected boolean enabled = true;
    @Getter
    @Setter
    protected String name;
    @Getter
    @Setter
    protected String initialValue;
    @Getter
    @Setter
    protected String currentValue;
    @Getter
    protected UUID uuid;


    public UserVariableDTO() {
        uuid = UUID.randomUUID();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return ObjectUtils.isEmpty(getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserVariableDTO that = (UserVariableDTO) o;
        return Objects.equals(getCollectionId(), that.getCollectionId()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCollectionId(), getName());
    }

    public Param toParam() {
        if (StringUtils.hasLength(currentValue)) {
            return new Param(name, currentValue, enabled);
        } else {
            return new Param(name, initialValue, enabled);
        }
    }
}

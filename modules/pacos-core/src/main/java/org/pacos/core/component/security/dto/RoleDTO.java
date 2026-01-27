package org.pacos.core.component.security.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = { "id", "label", "description" })
public class RoleDTO {

    private Integer id;
    private String label;
    private String description;

}

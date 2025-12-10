package org.pacos.core.component.dock.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Data transport object for DockConfiguration entity
 */
@EqualsAndHashCode(of = "id", callSuper = false)
public class DockConfigurationDTO {

    @Getter
    private final int id;
    @Getter
    @Setter
    private int userId;
    @Getter
    @Setter
    private String activator;
    @Getter
    @Setter
    private int orderNum;

    public DockConfigurationDTO(int id) {
        this.id = id;
    }

}

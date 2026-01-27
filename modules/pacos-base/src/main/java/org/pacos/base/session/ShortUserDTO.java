package org.pacos.base.session;

import java.io.Serializable;
import java.util.List;

/**
 * This class is responsible to stores basic user information for display purpose
 */
public record ShortUserDTO(Integer id, String name, List<String> roleNames) implements Serializable {

    public String getRoleString() {
        return String.join(", ", roleNames);
    }
}
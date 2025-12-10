package org.pacos.base.session;

import java.io.Serializable;

/**
 * This class is responsible to stores basic user information for display purpose
 */
public record ShortUserDTO(Integer id, String name) implements Serializable {

}
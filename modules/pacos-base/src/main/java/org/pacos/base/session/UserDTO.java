package org.pacos.base.session;

import java.io.Serializable;
import java.util.Objects;

/**
 * Stored basic user information for session purpose
 */
public class UserDTO implements Serializable {

    public static final int GUEST_ID = 1;
    public static final int ADMIN_ID = 2;

    private final int id;
    private final String userName;
    private Integer variableCollectionId;

    public UserDTO(int id, String userName, Integer variableCollectionId) {
        this.userName = userName;
        this.id = id;
        this.variableCollectionId = variableCollectionId;
    }

    public UserDTO(String userName) {
        this.id = GUEST_ID;
        this.userName = userName;
    }

    public boolean isGuestSession() {
        return id == GUEST_ID;
    }

    public boolean isAdminSession() {
        return id == ADMIN_ID;
    }

    public void setVariableCollectionId(Integer variableCollectionId) {
        this.variableCollectionId = variableCollectionId;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getVariableCollectionId() {
        return variableCollectionId;
    }

    @Override
    public String toString() {
        return "UserDTO={" +
                "id='" + id + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return getId() == userDTO.getId() && Objects.equals(getUserName(), userDTO.getUserName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserName());
    }
}

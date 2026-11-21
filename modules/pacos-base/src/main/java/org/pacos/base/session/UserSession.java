package org.pacos.base.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.pacos.base.event.UISystem;
import org.pacos.base.security.Permission;
import org.pacos.base.security.PermissionName;
import org.pacos.base.security.SecurityManager;
import org.pacos.base.utils.notification.NotificationUtils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

/**
 * This object represents the user's session.
 * It provides the ability to associate any data with a given session
 * Contains also information about opened browser window
 */
public class UserSession implements SecurityManager, Serializable {

    /* Session owner */
    private final transient UserDTO user;
    /* User session data */
    private final Map<String, Object> objectMap = new HashMap<>();

    /*UI map. Each browser window is a single entry in this map */
    private final Map<UI, UISystem> uiSystems = new HashMap<>();
    private final Set<PermissionName> permissions;
    private final Set<Integer> roles;

    public UserSession(UserDTO user) {
        this(user, Set.of(), Set.of());
    }

    public UserSession(UserDTO user, Set<PermissionName> permissions) {
        this(user, permissions, Set.of());
    }

    public UserSession(UserDTO user, Set<PermissionName> permissions, Set<Integer> roles) {
        this.user = user;
        this.permissions = Set.copyOf(permissions);
        this.roles = Set.copyOf(roles);
    }

    public static UserSession getCurrent() {
        return VaadinSession.getCurrent().getAttribute(UserSession.class);
    }

    public int getUserId() {
        return user.getId();
    }

    public void setUiSystem(UISystem uiSystem) {
        this.uiSystems.put(UI.getCurrent(), uiSystem);
    }

    public UISystem getUISystem() {
        return uiSystems.get(UI.getCurrent());
    }

    public UISystem getUISystem(UI ui) {
        return uiSystems.get(ui);
    }

    public Collection<UISystem> getUISystems() {
        return uiSystems.values();
    }

    public Set<UI> getUIs() {
        return uiSystems.keySet().stream().filter(ui -> ui.isVisible() && ui.isAttached()).collect(Collectors.toSet());
    }

    public String getUserName() {
        return user.getUserName();
    }

    public void addToSession(String key, Object value) {
        objectMap.put(key, value);
    }

    public Object getFromSession(String key) {
        return objectMap.get(key);
    }

    public void removeFromSession(String key) {
        objectMap.remove(key);
    }

    public UserDTO getUser() {
        return user;
    }

    public Set<Integer> getRoles() {
        return roles;
    }

    public boolean hasRole(Integer roleId) {
        return roles.contains(roleId);
    }

    public Set<PermissionName> getPermissions() {
        return permissions;
    }

    public void restoreUISystem(UI detachedUI) {
        if (detachedUI == null) {
            return;
        }
        UISystem system = uiSystems.get(detachedUI);
        if (system != null) {
            uiSystems.put(UI.getCurrent(), system);
        }
    }

    public boolean hasActionPermission(Permission permission) {
        boolean result = hasPermission(permission);
        if (!result && VaadinSession.getCurrent() != null) {
            NotificationUtils.info("You don't have permission for this action: " + permission.getKey());
        }
        return result;
    }

    public boolean hasPermission(Permission permission) {
        UserDTO currentUser = getUser();
        return currentUser != null && (currentUser.isAdminSession() || permissions.stream().anyMatch(e ->
                e.key().equals(permission.getKey())));
    }
}
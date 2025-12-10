package org.pacos.core.config.session;

import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.Cookie;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.pacos.base.security.PermissionConfig;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.security.service.UserPermissionService;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.component.user.service.UserService;
import org.pacos.core.system.cookie.CookieUtils;
import org.pacos.core.system.view.login.LoginForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSessionService {

    private static final Logger LOG = LoggerFactory.getLogger(UserSessionService.class);
    private static int guestCounter = 1;
    private final UserService userService;
    private final UserPermissionService userPermissionService;

    @Autowired
    public UserSessionService(UserService userService, UserPermissionService userPermissionService) {
        this.userService = userService;
        this.userPermissionService = userPermissionService;
    }

    public static boolean isLogIn(UserProxyService proxyService) {
        boolean logIn = VaadinSession.getCurrent().getAttribute(UserSession.class) != null;
        if (!logIn && proxyService.isSingleUserMode()) {
            Optional<UserDTO> userDTO = proxyService.loadUserByLogin("admin");
            createSessionFoUser(userDTO, proxyService.getUserPermissionService());
            return true;
        }
        return logIn;
    }

    public static void logOut() {
        VaadinSession session = VaadinSession.getCurrent();
        CookieUtils.removeCookie(CookieUtils.TOKEN);
        destroyUserSession();
        session.close();
    }

    public void initializeGuestSession() {

        if (VaadinSession.getCurrent().getAttribute(UserSession.class) == null) {
            int number = guestCounter++;
            VaadinSession.getCurrent()
                    .setAttribute(UserSession.class,
                            new UserSession(new UserDTO("Guest " + number),
                                    userService.loadUserPermission(UserDTO.GUEST_ID)));
        }
    }

    public static void destroyUserSession() {
        if (VaadinSession.getCurrent().getAttribute(UserSession.class) != null) {
            UserSession session = VaadinSession.getCurrent().getAttribute(UserSession.class);
            if (UI.getCurrent() != null
                    && session != null
                    && session.getUISystem() != null
                    && session.getUISystem().getWindowManager() != null) {
                session.getUISystem().getWindowManager().detachAll();
                LOG.debug("User logout with success {}", session.getUser());
            }
            VaadinSession.getCurrent().setAttribute(UserSession.class, null);
        }
    }

    public static boolean autoLogin(UserProxyService proxyService) {
        Optional<Cookie> token = CookieUtils.getCookie(CookieUtils.TOKEN);
        if (token.isEmpty()) {
            return false;
        }
        String tokenValue = token.get().getValue();
        Optional<UserDTO> userDTO = proxyService.loadUserByToken(tokenValue);
        createSessionFoUser(userDTO, proxyService.getUserPermissionService());
        return userDTO.isPresent();
    }

    private static void createSessionFoUser(Optional<UserDTO> userDTO, UserPermissionService permissionService) {
        if (userDTO.isPresent()) {
            Set<PermissionConfig> permissions = permissionService.getUserPermissions(userDTO.get().getId());
            VaadinSession.getCurrent()
                    .setAttribute(UserSession.class, new UserSession(userDTO.get(), permissions));
            LOG.debug("Session initialized for user {}", userDTO);
        }
    }

    public boolean initializeSession(LoginForm loginForm) {
        if (!userService.isValidCredentials(loginForm)) {
            return false;
        }

        if (VaadinSession.getCurrent().getAttribute(UserSession.class) == null) {
            Optional<UserDTO> userDTO = userService.loadUserByLogin(loginForm.getLogin());
            createSessionFoUser(userDTO, userPermissionService);
        }

        return VaadinSession.getCurrent().getAttribute(UserSession.class) != null;
    }

    public void storeToken(String token) {
        UserSession userSession = UserSession.getCurrent();
        userService.saveToken(token, userSession.getUser());
    }

    public boolean isValidCredentials(LoginForm loginForm) {
        return userService.isValidCredentials(loginForm);
    }
}

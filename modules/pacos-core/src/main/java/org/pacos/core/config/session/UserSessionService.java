package org.pacos.core.config.session;

import java.util.Optional;
import java.util.Set;

import org.pacos.base.security.PermissionName;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.pacos.core.system.cookie.CookieUtils;
import org.pacos.core.system.view.login.LoginForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;

@Service
public class UserSessionService {

    private static final Logger LOG = LoggerFactory.getLogger(UserSessionService.class);
    private static int guestCounter = 1;
    private final UserProxyService userProxyService;

    @Autowired
    public UserSessionService(UserProxyService userProxyService) {
        this.userProxyService = userProxyService;
    }

    public static boolean isLogIn(UserProxyService proxyService) {
        boolean logIn = VaadinSession.getCurrent().getAttribute(UserSession.class) != null;
        if (!logIn && proxyService.isSingleUserMode()) {
            Optional<UserDTO> userDTO = proxyService.loadUserByLogin("admin");
            createSessionFoUser(userDTO, proxyService);
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
                                    userProxyService.loadUserPermissions(UserDTO.GUEST_ID)));
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
        createSessionFoUser(userDTO, proxyService);
        return userDTO.isPresent();
    }

    private static void createSessionFoUser(Optional<UserDTO> userDTO, UserProxyService UserProxyService) {
        if (userDTO.isPresent()) {
            Set<PermissionName> userPermissions = UserProxyService.loadUserPermissions(userDTO.get().getId());
            UserSession userSession = new UserSession(userDTO.get(), userPermissions);
            VaadinSession.getCurrent()
                    .setAttribute(UserSession.class, userSession);
            LOG.debug("Session initialized for user {}", userDTO);
        }
    }

    public boolean initializeSession(LoginForm loginForm) {
        if (!userProxyService.isValidCredentials(loginForm)) {
            return false;
        }

        if (VaadinSession.getCurrent().getAttribute(UserSession.class) == null) {
            Optional<UserDTO> userDTO = userProxyService.loadUserByLogin(loginForm.getLogin());
            createSessionFoUser(userDTO, userProxyService);
        }

        return VaadinSession.getCurrent().getAttribute(UserSession.class) != null;
    }

    public void storeToken(String token) {
        UserSession userSession = UserSession.getCurrent();
        userProxyService.saveToken(token, userSession.getUser());
    }

    public boolean isValidCredentials(LoginForm loginForm) {
        return userProxyService.isValidCredentials(loginForm);
    }
}

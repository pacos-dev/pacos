package org.pacos.core.config.session;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;

@Service
public class UserSessionService {

    private static final Logger LOG = LoggerFactory.getLogger(UserSessionService.class);
    private static final AtomicInteger GUEST_COUNTER = new AtomicInteger(1);
    private final UserProxyService userProxyService;

    @Autowired
    public UserSessionService(UserProxyService userProxyService) {
        this.userProxyService = userProxyService;
    }

    public static boolean isLogIn(UserProxyService proxyService) {
        boolean logIn = VaadinSession.getCurrent().getAttribute(UserSession.class) != null;
        if (!logIn && proxyService.isSingleUserMode()) {
            Optional<UserDTO> userDTO = proxyService.loadUserByLogin("admin");
            createSessionForUser(userDTO, proxyService);
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

    /**
     * Invalidates a Vaadin session when its UI system is in an unrecoverable state.
     *
     * @param reason diagnostic reason for invalidating the session
     */
    public static void invalidateCorruptedSession(String reason) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession == null) {
            LOG.error("Cannot invalidate corrupted session because no VaadinSession is bound. Reason: {}", reason);
            return;
        }

        WrappedSession httpSession = vaadinSession.getSession();
        UserSession userSession = vaadinSession.getAttribute(UserSession.class);
        String userName = userSession == null ? "unknown" : userSession.getUserName();
        UI ui = UI.getCurrent();
        int uiId = ui == null ? -1 : ui.getUIId();

        LOG.error("Invalidating corrupted PacOS session. user={}, uiId={}, reason={}",
                userName, uiId, reason);

        logOut();

        if (ui != null) {
            ui.getPage().setLocation("login");
        }

        httpSession.invalidate();
    }

    public void initializeGuestSession() {
        if (VaadinSession.getCurrent().getAttribute(UserSession.class) == null) {
            int number = GUEST_COUNTER.getAndIncrement();
            createSessionForUser(Optional.of(new UserDTO("Guest " + number)), userProxyService);
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
        createSessionForUser(userDTO, proxyService);
        return userDTO.isPresent();
    }

    private static void createSessionForUser(Optional<UserDTO> userDTO, UserProxyService userProxyService) {
        if (userDTO.isPresent()) {
            int userId = userDTO.get().getId();
            Set<Integer> userRoles = userProxyService.loadUserRoles(userId);
            Set<PermissionName> userPermissions = userProxyService.loadUserPermissions(userId, userRoles);
            UserSession userSession = new UserSession(userDTO.get(), userPermissions, userRoles);
            VaadinSession.getCurrent().setAttribute(UserSession.class, userSession);
            LOG.debug("Session initialized for user {}", userDTO);
        }
    }

    public boolean initializeSession(LoginForm loginForm) {
        if (!userProxyService.isValidCredentials(loginForm)) {
            return false;
        }

        if (VaadinSession.getCurrent().getAttribute(UserSession.class) == null) {
            Optional<UserDTO> userDTO = userProxyService.loadUserByLogin(loginForm.getLogin());
            createSessionForUser(userDTO, userProxyService);
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
package org.pacos.core.component.session.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import org.pacos.base.event.ModuleEvent;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.VariableManager;
import org.pacos.core.config.session.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.variablefield.provider.VariableProvider;

/**
 * Configured in file 'com.vaadin.flow.server.VaadinServiceInitListener' and initialized by vaadin engine
 * This class collect all vaadin session and allows operation on top of it
 */
public class ServiceListener implements VaadinServiceInitListener {

    static final Set<VaadinSession> allSessions = new HashSet<>();

    private static final Logger LOG = LoggerFactory.getLogger(ServiceListener.class);
    private static VaadinService vaadinService;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        setVaadinService(event.getSource());
        event.getSource().addSessionInitListener(initEvent -> sessionInitListener());

        event.getSource().addSessionDestroyListener(destroyEvent -> sessionDestroyListener());
    }

    private static void setVaadinService(VaadinService vaadinService) {
        ServiceListener.vaadinService = vaadinService;
    }


    public static void notifyAll(ModuleEvent event, Object value) {
        for (VaadinSession session : allSessions) {
            try {
                session.lock();
                UserSession userSession = session.getAttribute(UserSession.class);
                if (userSession != null) {
                    userSession.getUISystems().forEach(uiSystem -> uiSystem.notify(event, value));
                }
            } finally {
                session.unlock();
            }
        }
    }

    public static Registration addRequestHandler(RequestHandler requestHandler) {
        Registration registration = vaadinService.addSessionInitListener(event ->
                event.getSession().addRequestHandler(requestHandler));
        allSessions.forEach(session -> {
            session.lock();
            session.addRequestHandler(requestHandler);
            session.unlock();
        });
        return registration;
    }

    public static void removeRequestHandler(RequestHandler requestHandler) {
        allSessions.forEach(session -> {
            session.lock();
            session.removeRequestHandler(requestHandler);
            session.unlock();
        });
    }

    public static void addVaadinSession(VaadinSession current) {
        allSessions.add(current);
    }

    public static void addVariableProviders(Set<VariableProvider> variableProviders) {
        allSessions.forEach(session -> {
            session.lock();
            UserSession userSession = session.getAttribute(UserSession.class);
            if (userSession != null) {
                userSession.getUISystems().forEach(uiSystem ->
                        variableProviders.forEach(variableProvider -> {
                            if (uiSystem.getVariableManager() != null) {
                                uiSystem.getVariableManager().addProvider(variableProvider);
                            }
                        }));
            }
            session.unlock();
        });
    }

    public static void removeVariableProviders(Set<VariableProvider> variableProviders) {
        allSessions.forEach(session -> {
            session.lock();
            UserSession userSession = session.getAttribute(UserSession.class);
            if (userSession != null) {
                userSession.getUIs().forEach(ui -> ui.access(() -> {
                    variableProviders.forEach(variableProvider -> {
                        VariableManager variableManager = userSession.getUISystem(ui).getVariableManager();
                        if (variableManager != null) {
                            variableManager.removeProvider(variableProvider);
                        }
                    });
                    ui.push();
                }));
            }
            session.unlock();
        });
    }


    static void sessionDestroyListener() {
        LOG.info("A session has been destroyed! {}", VaadinSession.getCurrent());
        allSessions.remove(VaadinSession.getCurrent());
        UserSessionService.destroyUserSession();
    }

    static void sessionInitListener() {
        allSessions.add(VaadinSession.getCurrent());
        VaadinSession.getCurrent().setAttribute("initialized", LocalDateTime.now());
        LOG.info("A new Vaadin Session has been initialized {}",
                VaadinSession.getCurrent().getBrowser().getAddress());
    }
}

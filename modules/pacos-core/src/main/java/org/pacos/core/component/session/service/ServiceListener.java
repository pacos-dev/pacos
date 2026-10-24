package org.pacos.core.component.session.service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import org.pacos.base.event.ModuleEvent;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.manager.VariableManager;
import org.pacos.core.config.session.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.variablefield.provider.VariableProvider;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

/**
 * Configured in file 'com.vaadin.flow.server.VaadinServiceInitListener' and initialized by vaadin engine
 * This class collect all vaadin session and allows operation on top of it
 */
public class ServiceListener implements VaadinServiceInitListener {

    static final Set<VaadinSession> allSessions = new CopyOnWriteArraySet<>();

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
        forEachSessionLocked("notify UI systems about " + event, session -> {
            UserSession userSession = session.getAttribute(UserSession.class);
            if (userSession == null) {
                return;
            }

            userSession.getUISystems().forEach(uiSystem -> {
                try {
                    uiSystem.notify(event, value);
                } catch (RuntimeException exception) {
                    LOG.error("Failed to notify UI system about {} for Vaadin session {}", event, session, exception);
                }
            });
        });
    }

    public static Registration addRequestHandler(RequestHandler requestHandler) {
        Registration registration = vaadinService.addSessionInitListener(event ->
                event.getSession().addRequestHandler(requestHandler));
        forEachSessionLocked("add request handler", session -> session.addRequestHandler(requestHandler));
        return registration;
    }

    public static void removeRequestHandler(RequestHandler requestHandler) {
        forEachSessionLocked("remove request handler", session -> session.removeRequestHandler(requestHandler));
    }

    public static void addVaadinSession(VaadinSession current) {
        allSessions.add(current);
    }

    public static void addVariableProviders(Set<VariableProvider> variableProviders) {
        forEachSessionLocked("add variable providers", session -> {
            UserSession userSession = session.getAttribute(UserSession.class);
            if (userSession == null) {
                return;
            }

            userSession.getUISystems().forEach(uiSystem -> {
                VariableManager variableManager = uiSystem.getVariableManager();
                if (variableManager == null) {
                    return;
                }
                variableProviders.forEach(variableProvider -> {
                    try {
                        variableManager.addProvider(variableProvider);
                    } catch (RuntimeException exception) {
                        LOG.error("Failed to add variable provider to a UI system", exception);
                    }
                });
            });
        });
    }

    public static void removeVariableProviders(Set<VariableProvider> variableProviders) {
        forEachSessionLocked("schedule variable provider removal", session -> {
            UserSession userSession = session.getAttribute(UserSession.class);
            if (userSession == null) {
                return;
            }

            userSession.getUIs().forEach(ui -> {
                try {
                    ui.access(() -> removeVariableProvidersFromUi(ui, userSession, variableProviders));
                } catch (RuntimeException exception) {
                    LOG.error("Failed to schedule variable provider removal for UI {}", ui, exception);
                }
            });
        });
    }

    private static void removeVariableProvidersFromUi(UI ui, UserSession userSession,
            Set<VariableProvider> variableProviders) {
        try {
            UISystem uiSystem = userSession.getUISystem(ui);
            if (uiSystem == null) {
                return;
            }

            VariableManager variableManager = uiSystem.getVariableManager();
            if (variableManager != null) {
                variableProviders.forEach(variableProvider -> {
                    try {
                        variableManager.removeProvider(variableProvider);
                    } catch (RuntimeException exception) {
                        LOG.error("Failed to remove variable provider from a UI", exception);
                    }
                });
            }
        } catch (RuntimeException exception) {
            LOG.error("Failed to remove variable providers from UI {}", ui, exception);
        } finally {
            try {
                ui.push();
            } catch (RuntimeException exception) {
                LOG.error("Failed to push UI {} after variable provider removal", ui, exception);
            }
        }
    }

    private static void forEachSessionLocked(String operation, Consumer<VaadinSession> action) {
        for (VaadinSession session : allSessions) {
            boolean locked = false;
            try {
                session.lock();
                locked = true;
                action.accept(session);
            } catch (RuntimeException exception) {
                LOG.error("Failed to {} for Vaadin session {}", operation, session, exception);
            } finally {
                if (locked) {
                    try {
                        session.unlock();
                    } catch (RuntimeException exception) {
                        LOG.error("Failed to unlock Vaadin session {} after {}", session, operation, exception);
                    }
                }
            }
        }
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

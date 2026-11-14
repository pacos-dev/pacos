package org.pacos.core.component.plugin.manager;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;
import org.pacos.core.component.credential.service.CredentialVaultService;
import org.pacos.core.component.plugin.manager.data.PluginJar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import ch.qos.logback.classic.Logger;

class PluginRuntimeFactoryTest {

    @Test
    void whenCallCreateContextThenConfigureAndRefreshIsolatedContext() throws Exception {
        ApplicationContext coreContext = mock(ApplicationContext.class);
        when(coreContext.getBeanNamesForType(CredentialVaultService.class)).thenReturn(new String[0]);
        PluginJar pluginJar = mock(PluginJar.class);
        URLClassLoader classLoader = mock(URLClassLoader.class);
        when(pluginJar.getPluginClassLoader()).thenReturn(classLoader);
        PluginRuntimeFactory factory = new PluginRuntimeFactory(coreContext);
        Logger moduleLogger = mock(ch.qos.logback.classic.Logger.class);

        try (var loggers = mockConstruction(ModuleLogger.class, (logger, context) ->
                     when(logger.getLogger()).thenReturn(moduleLogger));
             var contexts = mockConstruction(AnnotationConfigApplicationContext.class)) {
            ConfigurableApplicationContext result = factory.createContext(pluginJar, "sample");

            AnnotationConfigApplicationContext context = contexts.constructed().getFirst();
            assertSame(context, result);
            var order = inOrder(context, loggers.constructed().getFirst());
            order.verify(context).setClassLoader(classLoader);
            order.verify(context).setParent(coreContext);
            order.verify(context).scan("org.pacos.plugin.sample.config");
            order.verify(context).registerBean(RequestMappingHandlerMapping.class);
            order.verify(context).refresh();
            order.verify(loggers.constructed().getFirst()).stopLogger();
        }
    }

    @Test
    void whenCallCreateContextAndRefreshFailsThenCloseContextAndStopLogger() throws Exception {
        ApplicationContext coreContext = mock(ApplicationContext.class);
        when(coreContext.getBeanNamesForType(CredentialVaultService.class)).thenReturn(new String[0]);
        PluginJar pluginJar = mock(PluginJar.class);
        when(pluginJar.getPluginClassLoader()).thenReturn(mock(URLClassLoader.class));
        PluginRuntimeFactory factory = new PluginRuntimeFactory(coreContext);
        RuntimeException refreshFailure = new IllegalStateException("refresh failed");
        RuntimeException closeFailure = new IllegalStateException("close failed");
        ch.qos.logback.classic.Logger moduleLogger = mock(ch.qos.logback.classic.Logger.class);

        try (var loggers = mockConstruction(ModuleLogger.class, (logger, context) ->
                     when(logger.getLogger()).thenReturn(moduleLogger));
             var contexts = mockConstruction(AnnotationConfigApplicationContext.class, (context, construction) -> {
                 doThrow(refreshFailure).when(context).refresh();
                 doThrow(closeFailure).when(context).close();
             })) {
            assertSame(refreshFailure, assertThrows(IllegalStateException.class,
                    () -> factory.createContext(pluginJar, "sample")));

            AnnotationConfigApplicationContext context = contexts.constructed().getFirst();
            assertArrayEquals(new Throwable[] { closeFailure }, refreshFailure.getSuppressed());
            var order = inOrder(context, loggers.constructed().getFirst());
            order.verify(context).refresh();
            order.verify(context).close();
            order.verify(loggers.constructed().getFirst()).stopLogger();
        }
    }
}

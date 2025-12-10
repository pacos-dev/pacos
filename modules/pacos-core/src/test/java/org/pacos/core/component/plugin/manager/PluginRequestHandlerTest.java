package org.pacos.core.component.plugin.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.util.Set;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PluginRequestHandlerTest {

    @Test
    void whenHandlerDoNotContainsInformationAboutResourceThenReturnFalse() throws IOException {
        PluginRequestHandler handler =
                new PluginRequestHandler(Set.of("/test.css"), Mockito.mock(URLClassLoader.class));

        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        when(request.getPathInfo()).thenReturn("/icon.png");
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        VaadinSession session = Mockito.mock(VaadinSession.class);

        //then
        assertFalse(handler.handleRequest(session, request, response));
    }

    @Test
    void whenHandlerContainsInformationAboutResourceThenReturnTrue() throws IOException {
        URLClassLoader classLoader = Mockito.mock(URLClassLoader.class);
        PluginRequestHandler handler = new PluginRequestHandler(Set.of("/empty_file.txt"), classLoader);

        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        when(request.getPathInfo()).thenReturn("/empty_file.txt");
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        when(response.getOutputStream()).thenReturn(Mockito.mock(OutputStream.class));
        when(classLoader.getResourceAsStream("META-INF/resources/empty_file.txt")).thenReturn(Mockito.mock(InputStream.class));
        VaadinSession session = Mockito.mock(VaadinSession.class);

        //then
        assertTrue(handler.handleRequest(session, request, response));
    }

    @Test
    void whenHandlerContainsInformationAboutResourceButCantBeFoundInClasspathThenReturnFalse() throws IOException {
        URLClassLoader classLoader = Mockito.mock(URLClassLoader.class);
        PluginRequestHandler handler = new PluginRequestHandler(Set.of("/empty_file.txt"), classLoader);

        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        when(request.getPathInfo()).thenReturn("/empty_file.txt");
        VaadinResponse response = Mockito.mock(VaadinResponse.class);
        when(response.getOutputStream()).thenReturn(Mockito.mock(OutputStream.class));
        VaadinSession session = Mockito.mock(VaadinSession.class);

        //then
        assertFalse(handler.handleRequest(session, request, response));
    }
}
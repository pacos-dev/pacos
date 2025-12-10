package org.pacos.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApplicationTest {

    @Test
    void whenGetSpringApplicationThenNoException(){
        assertDoesNotThrow(Application::getSpringApplication);
    }

    @Test
    void whenInitializeClassThenNoException(){
        assertDoesNotThrow(Application::new);
    }
}
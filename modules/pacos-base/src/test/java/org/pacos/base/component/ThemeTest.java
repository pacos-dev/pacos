package org.pacos.base.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ThemeTest {

    @Test
    void whenGetNameThenAllThemeHasConfiguredName(){
        for(Theme theme : Theme.values()){
            assertNotNull(theme.getName());
            assertFalse(theme.getName().isEmpty());
        }
    }
}
package org.pacos.base.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SpinnerTest {

    @Test
    void whenCenterCalledThenReturnSameSpinnerAndSetMargin() {
        Spinner spinner = new Spinner();

        Spinner result = spinner.center();

        assertSame(spinner, result);
        assertEquals("auto", spinner.getStyle().get("margin"));
    }

    @Test
    void whenAppLoaderCalledThenSetExpectedSizeAndStyles() {
        Spinner spinner = new Spinner();

        Spinner result = spinner.appLoader();

        assertSame(spinner, result);
        assertEquals("40px", spinner.getWidth());
        assertEquals("40px", spinner.getHeight());
        assertEquals("30px", spinner.getStyle().get("margin-left"));
        assertEquals("10px", spinner.getStyle().get("margin-bottom"));
        assertEquals("4px solid var(--lumo-primary-text-color)", spinner.getStyle().get("border"));
        assertEquals("transparent", spinner.getStyle().get("border-bottom-color"));
    }
}

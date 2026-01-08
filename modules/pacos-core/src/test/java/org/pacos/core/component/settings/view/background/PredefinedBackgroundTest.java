package org.pacos.core.component.settings.view.background;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PredefinedBackgroundTest {

    @Test
    void whenValuesIsCalledThenReturnNineElements() {
        //when
        int count = PredefinedBackground.values().length;

        //then
        assertEquals(9, count);
    }

    @ParameterizedTest
    @CsvSource({
            "ONE, img/wallpaper/1.jpeg, 1920x1080",
            "SEVEN, img/wallpaper/7.jpeg, 2560x1700",
            "NINE, img/wallpaper/9.jpeg, 2560x1700"
    })
    void whenGettersAreCalledThenReturnCorrectValues(String enumName, String expectedSrc, String expectedRes) {
        //given
        PredefinedBackground background = PredefinedBackground.valueOf(enumName);

        //when
        String actualSrc = background.getSrc();
        String actualRes = background.getResolution();

        //then
        assertEquals(expectedSrc, actualSrc);
        assertEquals(expectedRes, actualRes);
    }

    @Test
    void whenGetSrcIsCalledForOneThenReturnCorrectPath() {
        //given
        PredefinedBackground background = PredefinedBackground.ONE;

        //when
        String result = background.getSrc();

        //then
        assertEquals("img/wallpaper/1.jpeg", result);
    }

    @Test
    void whenGetResolutionIsCalledForSevenThenReturnHigherResolution() {
        //given
        PredefinedBackground background = PredefinedBackground.SEVEN;

        //when
        String result = background.getResolution();

        //then
        assertEquals("2560x1700", result);
    }
}
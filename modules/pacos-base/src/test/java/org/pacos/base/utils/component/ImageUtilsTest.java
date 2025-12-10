package org.pacos.base.utils.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageUtilsTest {

    @Test
    void whenImageUtilsIsCreatedThenItShouldHaveCorrectSrcAndAlt() {
        String src = "path/to/image.jpg";
        String alt = "Image description";

        ImageUtils image = new ImageUtils(src, alt);

        assertEquals(src, image.getSrc());
        assertTrue(image.getAlt().isPresent());
        assertEquals(alt, image.getAlt().get());
    }

    @Test
    void whenWidthIsSetThenImageShouldHaveCorrectWidth() {
        int width = 200;

        ImageUtils image = new ImageUtils("path/to/image.jpg");
        image.withWidth(width);

        assertEquals("200.0px", image.getWidth(), "Width should be correctly set.");
    }

    @Test
    void whenHeightIsSetThenImageShouldHaveCorrectHeight() {
        int height = 150;

        ImageUtils image = new ImageUtils("path/to/image.jpg");
        image.withHeight(height);

        assertEquals("150.0px", image.getHeight(), "Height should be correctly set.");
    }

    @Test
    void whenStyleIsSetThenImageShouldHaveCorrectStyle() {
        String property = "border";
        String value = "1px solid red";

        ImageUtils image = new ImageUtils("path/to/image.jpg");
        image.withStyle(property, value);

        assertEquals(value, image.getStyle().get(property), "Style should be correctly set.");
    }
}

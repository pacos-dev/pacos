package org.pacos.common.view.grid.decorator;

import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class BlurAndFocusDecoratorTest {

    @Test
    void whenConfigureForGridThenAddBlurAndFocusListener() {
        var grid = spy(new Grid<>());

        BlurAndFocusDecorator.configureFor(grid);

        verify(grid).addFocusListener(any());
        verify(grid).addBlurListener(any());
    }
}
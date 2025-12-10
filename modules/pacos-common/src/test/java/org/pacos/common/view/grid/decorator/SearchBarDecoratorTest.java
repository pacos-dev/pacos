package org.pacos.common.view.grid.decorator;

import com.vaadin.flow.component.grid.Grid;
import org.pacos.base.utils.component.TextFieldUtils;
import org.pacos.common.event.ColumnFilterEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchBarDecoratorTest {

    @Test
    void whenConfigureThenExtendColumnAboutHeaderWithFilterTextField() {
        var grid = new Grid<>(TestBean.class);
        Grid.Column<TestBean> column = grid.getColumnByKey("column");
        ColumnFilterEvent filter = value -> {
        };
        //when
        SearchBarDecorator.configureFor(grid, column, filter);
        //then
        assertEquals(2, grid.getHeaderRows().size());
        assertTrue(grid.getHeaderRows().get(1).getCell(column).getComponent() instanceof TextFieldUtils);
    }

    @Test
    void whenValueChangeThenCallListener() {
        var grid = new Grid<>(TestBean.class);
        Grid.Column<TestBean> column = grid.getColumnByKey("column");
        AtomicInteger counter = new AtomicInteger(0);
        ColumnFilterEvent filter = value -> counter.incrementAndGet();

        //when
        SearchBarDecorator.configureFor(grid, column, filter);
        //then
        assertTrue(grid.getHeaderRows().get(1).getCell(column).getComponent() instanceof TextFieldUtils);
        TextFieldUtils tf = (TextFieldUtils) grid.getHeaderRows().get(1).getCell(column).getComponent();
        tf.setValue("test");
        assertEquals(1, counter.get());
    }

    record TestBean(String column) {
    }

    ;
}
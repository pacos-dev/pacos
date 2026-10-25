package org.pacos.common.view.grid;

import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.data.provider.Setter;
import com.vaadin.flow.function.ValueProvider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridEditableNameExtensionTest {

    private Grid<TestBean> grid;
    private TestExtension extension;
    private TestBean first;
    private TestBean second;

    @BeforeEach
    void setUp() {
        first = new TestBean("First");
        second = new TestBean("Second");
        grid = new Grid<>();
        Grid.Column<TestBean> column = grid.addColumn(TestBean::getName);
        grid.setItems(first, second);
        extension = new TestExtension(grid, column);
    }

    @Test
    void whenEditItemCalledThenOpenEditor() {
        extension.editItem(first);

        assertTrue(grid.getEditor().isOpen());
    }

    @Test
    void whenEditAnotherItemCalledThenKeepEditorOpenForNewItem() {
        extension.editItem(first);
        extension.editItem(second);
        grid.getEditor().save();

        assertSame(second, extension.getChangedBean());
    }

    @Test
    void whenCloseCalledThenCloseEditor() {
        extension.editItem(first);

        extension.close();

        assertFalse(grid.getEditor().isOpen());
    }

    private static class TestExtension extends GridEditableNameExtension<TestBean> {

        private TestBean changedBean;

        private TestExtension(Grid<TestBean> grid, Grid.Column<TestBean> column) {
            super(grid, column);
        }

        @Override
        public Setter<TestBean, String> setValue() {
            return TestBean::setName;
        }

        @Override
        public ValueProvider<TestBean, String> getValue() {
            return TestBean::getName;
        }

        @Override
        public void changeEvent(TestBean bean) {
            changedBean = bean;
        }

        private TestBean getChangedBean() {
            return changedBean;
        }
    }

    private static class TestBean {

        private String name;

        private TestBean(String name) {
            this.name = name;
        }

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }
    }
}

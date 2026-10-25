package org.pacos.common.view.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GridEditableNameExtensionTest {

    private Grid<TestBean> grid;
    private Editor<TestBean> editor;
    private TestExtension extension;
    private TestBean first;
    private TestBean second;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        first = new TestBean("First");
        second = new TestBean("Second");
        grid = mock(Grid.class);
        editor = mock(Editor.class);
        when(grid.getEditor()).thenReturn(editor);
        Grid.Column<TestBean> column = mock(Grid.Column.class);
        extension = new TestExtension(grid, column);
    }

    @Test
    void whenEditItemCalledThenDelegateToEditor() {
        extension.editItem(first);

        verify(editor).editItem(first);
    }

    @Test
    void whenEditAnotherItemCalledThenCancelCurrentEditorBeforeOpeningNewItem() {
        when(editor.isOpen()).thenReturn(false, true);

        extension.editItem(first);
        extension.editItem(second);

        verify(editor).cancel();
        verify(editor).editItem(first);
        verify(editor).editItem(second);
    }

    @Test
    void whenCloseCalledThenCancelAndCloseOpenEditor() {
        when(editor.isOpen()).thenReturn(true);

        extension.close();

        verify(editor).cancel();
        verify(editor).closeEditor();
    }

    private static class TestExtension extends GridEditableNameExtension<TestBean> {

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

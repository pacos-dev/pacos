package org.pacos.base.component.filepicker;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pacos.base.file.FileInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilePickerDataProviderTest {

    private FilePickerDataProvider dataProvider;
    private FilePickerRow directoryRow;
    private FilePickerRow childRow;
    private FilePickerRow fileRow;

    @BeforeEach
    void setUp() {
        FileInfo dirInfo = mock(FileInfo.class);
        when(dirInfo.isDirectory()).thenReturn(true);

        FileInfo fileInfo = mock(FileInfo.class);
        when(fileInfo.isDirectory()).thenReturn(false);

        directoryRow = Mockito.spy(new FilePickerRow(dirInfo));
        childRow = Mockito.spy(new FilePickerRow(dirInfo));
        fileRow = Mockito.spy(new FilePickerRow(fileInfo));

        dataProvider = new FilePickerDataProvider(List.of(dirInfo, fileInfo), true,null);
    }

    @Test
    void whenFetchChildrenFromBackEndWithEmptyParentThenReturnsRootDirs() {
        HierarchicalQuery<FilePickerRow, Long> query = new HierarchicalQuery<>(null, null);
        Stream<FilePickerRow> result = dataProvider.fetchChildrenFromBackEnd(query);

        Set<FilePickerRow> resultSet = result.collect(Collectors.toSet());

        assertEquals(2, resultSet.size());
        assertTrue(resultSet.stream().anyMatch(e -> e.equals(directoryRow)));
        assertTrue(resultSet.stream().anyMatch(e -> e.equals(fileRow)));
    }


    @Test
    void whenFetchChildrenFromBackEndWithParentAndDisplayOnlyDirThenReturnsOnlyDirectories() {
        HierarchicalQuery<FilePickerRow, Long> query = new HierarchicalQuery<>(null, directoryRow);
        FilePickerDataProvider providerWithDirsOnly = new FilePickerDataProvider(List.of(directoryRow.fileInfo()), true,null);

        when(directoryRow.getChild()).thenReturn(Stream.of(directoryRow, fileRow));

        Stream<FilePickerRow> childrenStream = providerWithDirsOnly.fetchChildrenFromBackEnd(query);

        List<FilePickerRow> children = childrenStream.toList();

        assertEquals(1, children.size());
        assertEquals(directoryRow, children.get(0));
    }

    @Test
    void whenFetchChildrenFromBackEndWithParentAndDisplayAllThenReturnsAllChildren() {
        FilePickerDataProvider providerAllFiles = new FilePickerDataProvider(List.of(directoryRow.fileInfo()), false,null);
        HierarchicalQuery<FilePickerRow, Long> query = new HierarchicalQuery<>(null, directoryRow);

        when(directoryRow.getChild()).thenReturn(Stream.of(directoryRow, fileRow));

        Stream<FilePickerRow> childrenStream = providerAllFiles.fetchChildrenFromBackEnd(query);

        List<FilePickerRow> children = childrenStream.toList();

        assertEquals(2, children.size());
        assertTrue(children.contains(directoryRow));
        assertTrue(children.contains(fileRow));
    }


    @Test
    void whenFetchChildrenFromBackEndWithParentAndAllowedExtensionThenDisplayOnlyDir() {
        when(directoryRow.getName()).thenReturn("directory");
        when(directoryRow.isDirectory()).thenReturn(true);
        when(fileRow.getName()).thenReturn("test.log");
        when(directoryRow.getChild()).thenReturn(Stream.of(childRow,fileRow));

        FilePickerDataProvider providerAllFiles = new FilePickerDataProvider(List.of(directoryRow.fileInfo()), false,Set.of(".txt"));
        HierarchicalQuery<FilePickerRow, Long> query = new HierarchicalQuery<>(null, directoryRow);

        Stream<FilePickerRow> childrenStream = providerAllFiles.fetchChildrenFromBackEnd(query);

        List<FilePickerRow> children = childrenStream.toList();

        assertEquals(1, children.size());
        assertTrue(children.contains(childRow));
    }

    @Test
    void whenFetchChildrenFromBackEndWithParentAndAllowedExtensionThenDisplayFileWithMatchinExtension() {
        when(directoryRow.getName()).thenReturn("directory");
        when(directoryRow.isDirectory()).thenReturn(true);
        when(fileRow.getName()).thenReturn("test.txt");
        when(directoryRow.getChild()).thenReturn(Stream.of(childRow,fileRow));

        FilePickerDataProvider providerAllFiles = new FilePickerDataProvider(List.of(directoryRow.fileInfo()), false,Set.of(".txt"));
        HierarchicalQuery<FilePickerRow, Long> query = new HierarchicalQuery<>(null, directoryRow);

        Stream<FilePickerRow> childrenStream = providerAllFiles.fetchChildrenFromBackEnd(query);

        List<FilePickerRow> children = childrenStream.toList();

        assertEquals(2, children.size());
        assertTrue(children.contains(childRow));
        assertTrue(children.contains(fileRow));
    }


    @Test
    void whenGetChildCountWithEmptyParentThenReturnsRootDirCount() {
        HierarchicalQuery<FilePickerRow, Long> query = new HierarchicalQuery<>(null, null);
        int childCount = dataProvider.getChildCount(query);

        assertEquals(2, childCount);
    }

    @Test
    void whenGetChildCountWithParentAndDisplayOnlyDirThenReturnsOnlyDirectoryCount() {
        FilePickerDataProvider providerWithDirsOnly = new FilePickerDataProvider(List.of(directoryRow.fileInfo()), true,null);
        HierarchicalQuery<FilePickerRow, Long> query = new HierarchicalQuery<>(null, directoryRow);

        when(directoryRow.getChild()).thenReturn(Stream.of(directoryRow, fileRow));

        int childCount = providerWithDirsOnly.getChildCount(query);

        assertEquals(1, childCount);
    }

    @Test
    void whenHasChildrenWithDirectoryThenReturnsTrue() {
        when(directoryRow.getChild()).thenReturn(Stream.of(directoryRow, fileRow));

        assertTrue(dataProvider.hasChildren(directoryRow));
    }

    @Test
    void whenHasChildrenWithFileThenReturnsFalse() {
        when(fileRow.getChild()).thenReturn(Stream.empty());
        HierarchicalQuery<FilePickerRow, Long> query = new HierarchicalQuery<>(null, directoryRow);
        assertEquals(0, dataProvider.getChildCount(query));
        assertFalse(dataProvider.hasChildren(fileRow));
    }
}

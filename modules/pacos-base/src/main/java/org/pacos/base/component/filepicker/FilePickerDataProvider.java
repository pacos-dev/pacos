package org.pacos.base.component.filepicker;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.pacos.base.file.FileInfo;

public class FilePickerDataProvider extends AbstractBackEndHierarchicalDataProvider<FilePickerRow, Long> {

    private final Set<FilePickerRow> rootDirs;
    private final boolean displayOnlyDir;
    private final Set<String> allowedExtensions;

    public FilePickerDataProvider(List<FileInfo> rootDir, boolean displayOnlyDir,Set<String> allowedExtensions) {
        this.rootDirs = rootDir.stream().map(FilePickerRow::new).collect(Collectors.toSet());
        this.displayOnlyDir = displayOnlyDir;
        this.allowedExtensions = allowedExtensions;
    }

    @Override
    protected Stream<FilePickerRow> fetchChildrenFromBackEnd(HierarchicalQuery<FilePickerRow, Long> hierarchicalQuery) {
        if (hierarchicalQuery.getParentOptional().isEmpty()) {
            return rootDirs.stream();
        }
        return hierarchicalQuery.getParent().getChild().filter(this::isToDisplay);
    }

    @Override
    public int getChildCount(HierarchicalQuery<FilePickerRow, Long> hierarchicalQuery) {
        if (hierarchicalQuery.getParentOptional().isEmpty()) {
            return rootDirs.size();
        }
        return getChildCount(hierarchicalQuery.getParent());
    }


    @Override
    public boolean hasChildren(FilePickerRow fileInfo) {
        return getChildCount(fileInfo) > 0;
    }

    private int getChildCount(FilePickerRow parent) {
        return (int) parent.getChild().filter(this::isToDisplay).count();
    }

    private boolean isToDisplay(FilePickerRow e) {
        if (displayOnlyDir) {
            return e.isDirectory();
        }
        if(e.isDirectory()) {
            return true;
        }
        if(allowedExtensions == null){
            return true;
        }
        return !e.getName().isEmpty() &&
                e.getName().contains(".") &&
                allowedExtensions.contains(e.getName().substring(e.getName().lastIndexOf('.')));
    }

}

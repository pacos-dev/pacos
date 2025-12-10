package org.pacos.base.file;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.pacos.base.exception.PacosException;

/**
 * Represents file object
 */
public class FileInfo implements Serializable {

    protected final File file;
    protected long length;

    private FileInfo(File file) {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            throw new PacosException("File not exists: " + file.getPath());
        }
        this.file = file;
        this.length = file.length();
    }

    public static FileInfo of(Path path) {
        return new FileInfo(path.toFile());
    }

    public static FileInfo of(File file) {
        return new FileInfo(file);
    }

    public List<FileInfo> getChild() {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                return Arrays.stream(files).filter(File::exists).map(FileInfo::new).toList();
            }
        }
        return Collections.emptyList();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isFile() {
        return file.isFile();
    }

    public String getName() {
        if (file.getName().isEmpty()) {
            return file.getPath();
        }
        return file.getName();
    }

    public String getExtension() {
        if (getFile().isDirectory()) {
            return null;
        }
        final String[] split = getName().toLowerCase().split("\\.");
        return split.length > 1 ? split[split.length - 1] : "";
    }

    public File getFile() {
        return file;
    }

    public boolean isFileChanged() {
        return length != file.length();
    }

    public void updateFileLength() {
        this.length = file.length();
    }

    @Override
    public String toString() {
        return "FileInfo={" +
                "file='" + file + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(file, fileInfo.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

}

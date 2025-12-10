package org.pacos.base.file.archive;

public enum ArchiveType {
    ZIP(".zip"),
    TAR_GZ(".tar.gz"),
    GZIP(".gz");

    private final String extension;

    ArchiveType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public boolean isZip(){
        return this==ZIP;
    }

    public boolean isTarGz(){
        return this==TAR_GZ;
    }
}

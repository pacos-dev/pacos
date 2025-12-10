package org.pacos.base.file.archive;

public class ArchiveException extends RuntimeException {
    public ArchiveException(String msg) {
        super(msg);
    }

    public ArchiveException(Throwable e){
        super(e);
    }
}

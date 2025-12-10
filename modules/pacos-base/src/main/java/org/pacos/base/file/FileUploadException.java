package org.pacos.base.file;

public class FileUploadException extends RuntimeException {
    public FileUploadException(String msg, Exception e) {
        super(msg,e);
    }
}

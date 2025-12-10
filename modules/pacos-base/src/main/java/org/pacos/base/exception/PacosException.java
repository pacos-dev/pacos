package org.pacos.base.exception;

public class PacosException extends RuntimeException {

    public PacosException(String msg) {
        super(msg);
    }

    public PacosException(String msg, Throwable e) {
        super(msg, e);
    }

    public PacosException(Throwable e) {
        super(e);
    }
}

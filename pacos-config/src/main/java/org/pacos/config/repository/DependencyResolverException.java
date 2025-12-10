package org.pacos.config.repository;

public class DependencyResolverException extends RuntimeException {
    public DependencyResolverException(Exception e) {
        super(e);
    }

    public DependencyResolverException(String msg) {
        super(msg);
    }

    public DependencyResolverException(String msg, Exception e) {
        super(msg, e);
    }

}

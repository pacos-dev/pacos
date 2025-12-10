package org.pacos.core.config.database;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import com.vaadin.flow.server.VaadinSession;
import lombok.AccessLevel;
import lombok.Getter;
import org.pacos.base.session.UserSession;

@MappedSuperclass
@Getter(AccessLevel.PUBLIC)
public abstract class AbstractEntity implements Serializable {

    @Column(name = "creation_timestamp", nullable = false)
    private LocalDateTime creationTime;

    @Column(name = "update_timestamp", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_by", nullable = false)
    private Integer updatedBy;

    @PrePersist
    public void prePersist() {
        this.creationTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        if (VaadinSession.getCurrent() != null && UserSession.getCurrent() != null) {
            this.createdBy = UserSession.getCurrent().getUserId();
            this.updatedBy = UserSession.getCurrent().getUserId();
        } else {
            this.createdBy = 0;
            this.updatedBy = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
        if (VaadinSession.getCurrent() != null && UserSession.getCurrent() != null) {
            this.updatedBy = UserSession.getCurrent().getUserId();
        } else {
            this.updatedBy = 0;
        }
    }

}

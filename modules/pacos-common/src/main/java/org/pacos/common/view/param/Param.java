package org.pacos.common.view.param;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.ObjectUtils;

/**
 * Param representation. Equals and hashcode can't be overriden. Each param is unique especially when
 * id is not set
 */
public class Param implements Serializable {

    @JsonIgnore
    private Integer id;
    private String name;
    private String value;
    @JsonIgnore
    private String description;
    private boolean enabled;

    public Param() {
    }

    public Param(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Param(String name, String value, boolean enabled) {
        this.name = name;
        this.value = value;
        this.enabled = enabled;
    }

    public Param(Integer id,String name, String value, boolean enabled) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.enabled = enabled;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value != null ? value.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return ObjectUtils.isEmpty(name) && ObjectUtils.isEmpty(value) && ObjectUtils.isEmpty(description);
    }

    @JsonIgnore
    public boolean isEmptyParam() {
        return ObjectUtils.isEmpty(name) && ObjectUtils.isEmpty(value);
    }

    @Override
    public String toString() {
        return "Param{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                '}';
    }


    @JsonIgnore
    public boolean isAnyNull() {
        return getName() == null || getValue() == null;
    }
}
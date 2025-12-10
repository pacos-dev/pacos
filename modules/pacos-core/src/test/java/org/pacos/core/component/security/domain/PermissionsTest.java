package org.pacos.core.component.security.domain;

import org.junit.jupiter.api.Test;
import org.pacos.base.security.Permission;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionsTest {

    @Test
    void whenCreateEmptyConstructorThenFieldsAreNull() {
        Permissions action = new Permissions();
        assertThat(action.getKey()).isNull();
        assertThat(action.getLabel()).isNull();
        assertThat(action.getCategory()).isNull();
    }

    @Test
    void whenCreateFromPermissionDescriptorThenFieldsAreCopied() {
        Permission descriptor = new Permission() {

            @Override
            public String getKey() {
                return "mockserver.start";
            }

            @Override
            public String getLabel() {
                return "Start Mock Server";
            }

            @Override
            public String getCategory() {
                return "Mock Server";
            }

            @Override
            public String getDescription() {
                return "Awesome description";
            }
        };
        Permissions action = new Permissions(descriptor);

        assertThat(action.getKey()).isEqualTo("mockserver.start");
        assertThat(action.getLabel()).isEqualTo("Start Mock Server");
        assertThat(action.getCategory()).isEqualTo("Mock Server");
        assertThat(action.getDescription()).isEqualTo("Awesome description");
    }

    @Test
    void whenPermissionsHaveSameKeyThenTheyAreEqual() {
        Permissions a1 = new Permissions();
        a1.setKey("mockserver.start");
        Permissions a2 = new Permissions();
        a2.setKey("mockserver.start");

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    void whenPermissionsHaveDifferentKeysThenTheyAreNotEqual() {
        Permissions a1 = new Permissions();
        a1.setKey("mockserver.start");
        Permissions a2 = new Permissions();
        a2.setKey("mockserver.stop");

        assertThat(a1).isNotEqualTo(a2);
    }
}

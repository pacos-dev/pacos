package org.pacos.core.component.security.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AppPermissionTest {

    @Test
    void whenCreateEmptyConstructorThenFieldsAreNull() {
        AppPermission action = new AppPermission();
        assertThat(action.getKey()).isNull();
        assertThat(action.getLabel()).isNull();
        assertThat(action.getCategory()).isNull();
    }

    @Test
    void whenCreateFromPermissionDescriptorThenFieldsAreCopied() {
        org.pacos.base.security.Permission descriptor = new org.pacos.base.security.Permission() {

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
        AppPermission action = new AppPermission(descriptor);

        assertThat(action.getKey()).isEqualTo("mockserver.start");
        assertThat(action.getLabel()).isEqualTo("Start Mock Server");
        assertThat(action.getCategory()).isEqualTo("Mock Server");
        assertThat(action.getDescription()).isEqualTo("Awesome description");
    }

    @Test
    void whenPermissionsHaveSameKeyThenTheyAreEqual() {
        AppPermission a1 = new AppPermission();
        a1.setKey("mockserver.start");
        AppPermission a2 = new AppPermission();
        a2.setKey("mockserver.start");

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    void whenPermissionsHaveDifferentKeysThenTheyAreNotEqual() {
        AppPermission a1 = new AppPermission();
        a1.setKey("mockserver.start");
        AppPermission a2 = new AppPermission();
        a2.setKey("mockserver.stop");

        assertThat(a1).isNotEqualTo(a2);
    }
}

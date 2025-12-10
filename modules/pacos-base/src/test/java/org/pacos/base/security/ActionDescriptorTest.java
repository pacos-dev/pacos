package org.pacos.base.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionDescriptorTest {

    @Test
    void whenCreateActionDescriptorThenAllFieldsShouldBeSet() {
        ActionDescriptor descriptor = new ActionDescriptor("mockserver.start", "Start Mock Server", "Mock Server");
        assertThat(descriptor.key()).isEqualTo("mockserver.start");
        assertThat(descriptor.label()).isEqualTo("Start Mock Server");
        assertThat(descriptor.category()).isEqualTo("Mock Server");
    }

    @Test
    void whenTwoActionDescriptorsHaveSameValuesThenTheyAreEqual() {
        ActionDescriptor d1 = new ActionDescriptor("mockserver.start", "Start Mock Server", "Mock Server");
        ActionDescriptor d2 = new ActionDescriptor("mockserver.start", "Start Mock Server", "Mock Server");
        assertThat(d1).isEqualTo(d2);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }

    @Test
    void whenActionDescriptorsHaveDifferentValuesThenTheyAreNotEqual() {
        ActionDescriptor d1 = new ActionDescriptor("mockserver.start", "Start Mock Server", "Mock Server");
        ActionDescriptor d2 = new ActionDescriptor("mockserver.stop", "Stop Mock Server", "Mock Server");
        assertThat(d1).isNotEqualTo(d2);
    }
}

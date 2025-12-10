package org.pacos.core.component.plugin.manager.type;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginStatusEnumTest {

    @ParameterizedTest
    @EnumSource(PluginStatusEnum.class)
    void whenStatusIsOffOrErrorThenCanRunShouldReturnTrue(PluginStatusEnum status) {
        if (status == PluginStatusEnum.OFF || status == PluginStatusEnum.ERROR) {
            assertTrue(status.canRun());
        } else {
            assertFalse(status.canRun());
        }
    }

    @ParameterizedTest
    @EnumSource(PluginStatusEnum.class)
    void whenStatusIsOnInitializationOrShutdownThenCanStopShouldReturnTrue(PluginStatusEnum status) {
        if (status == PluginStatusEnum.ON || status == PluginStatusEnum.INITIALIZATION
                || status == PluginStatusEnum.SHUTDOWN) {
            assertTrue(status.canStop());
        } else {
            assertFalse(status.canStop());
        }
    }

    @ParameterizedTest
    @EnumSource(PluginStatusEnum.class)
    void whenStatusIsOnOffOrErrorThenCanProcessShouldReturnTrue(PluginStatusEnum status) {
        if (status == PluginStatusEnum.ON || status == PluginStatusEnum.OFF || status == PluginStatusEnum.ERROR) {
            assertTrue(status.canProcess());
        } else {
            assertFalse(status.canProcess());
        }
    }

    @ParameterizedTest
    @EnumSource(PluginStatusEnum.class)
    void whenStatusIsInitializationThenIsInitializedShouldReturnTrue(PluginStatusEnum status) {
        if (status == PluginStatusEnum.INITIALIZATION) {
            assertTrue(status.isInitialized());
        } else {
            assertFalse(status.isInitialized());
        }
    }

    @ParameterizedTest
    @EnumSource(PluginStatusEnum.class)
    void whenStatusIsOnThenIsOnShouldReturnTrue(PluginStatusEnum status) {
        if (status == PluginStatusEnum.ON) {
            assertTrue(status.isOn());
        } else {
            assertFalse(status.isOn());
        }
    }
}

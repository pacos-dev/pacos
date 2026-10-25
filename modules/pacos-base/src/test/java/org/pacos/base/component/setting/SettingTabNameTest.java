package org.pacos.base.component.setting;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingTabNameTest {

    @Test
    void whenGetNameCalledThenReturnExpectedNames() {
        List<String> names = Arrays.stream(SettingTabName.values())
                .map(SettingTabName::getName)
                .toList();

        assertEquals(List.of(
                "Plugins",
                "Dock",
                "Access management",
                "Users",
                "System",
                "Update",
                "Guest & Registration",
                "Background",
                "Restart",
                "API access",
                "Roles & Permissions",
                "Onboarding / Defaults"
        ), names);
    }
}

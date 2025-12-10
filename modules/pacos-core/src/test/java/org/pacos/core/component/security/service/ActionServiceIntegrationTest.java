package org.pacos.core.component.security.service;

import java.util.List;

import lombok.Getter;
import org.config.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pacos.base.security.Permission;
import org.pacos.core.component.security.repository.PermissionDefaultConfigRepository;
import org.pacos.core.component.security.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionServiceIntegrationTest extends IntegrationTestContext {

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PermissionDefaultConfigRepository defaultConfigRepository;

    @BeforeEach
    public void setUp() {
        defaultConfigRepository.deleteAllInBatch();
        permissionRepository.deleteAllInBatch();
    }

    @Test
    void whenAddNewKeysThenAdd2Entries() {
        long count = permissionRepository.count();
        //when
        int added = permissionService.resolvePermissionDefinition(List.of(TestPermissions.TEST, TestPermissions.TEST2));
        //then
        assertEquals(2, added);
        assertEquals(2, permissionRepository.count() - count);
        assertEquals(defaultConfigRepository.count(), defaultConfigRepository.count());
    }

    @Test
    void whenKeyAlreadyExistThenDoNotAddDuplicate() {
        //when
        int added = permissionService.resolvePermissionDefinition(List.of(TestPermissions.TEST3));
        //then
        assertEquals(1, added);
        //when
        added = permissionService.resolvePermissionDefinition(List.of(TestPermissions.TEST3));
        //then
        assertEquals(0, added);
    }

    @Getter
    enum TestPermissions implements Permission {

        TEST("test", "test", "test",""),
        TEST2("test1", "test", "test",""),
        TEST3("test3", "test", "test","");

        private final String key;
        private final String label;
        private final String category;
        private final String description;

        TestPermissions(String key, String label, String category,String description) {
            this.key = key;
            this.label = label;
            this.category = category;
            this.description = description;
        }

    }
}
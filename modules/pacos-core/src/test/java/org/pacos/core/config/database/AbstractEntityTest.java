package org.pacos.core.config.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AbstractEntityTest {

    @Test
    void whenPrePersistWithoutSessionThenTimestampsAndSystemUserAreSet() {
        TestEntity entity = new TestEntity();

        entity.prePersist();

        assertNotNull(entity.getCreationTime());
        assertNotNull(entity.getUpdateTime());
        assertEquals(0, entity.getCreatedBy());
        assertEquals(0, entity.getUpdatedBy());
    }

    @Test
    void whenPreUpdateWithoutSessionThenCreationTimeIsPreservedAndSystemUserIsSet() {
        TestEntity entity = new TestEntity();
        entity.prePersist();
        LocalDateTime creationTime = entity.getCreationTime();

        entity.preUpdate();

        assertEquals(creationTime, entity.getCreationTime());
        assertNotNull(entity.getUpdateTime());
        assertEquals(0, entity.getUpdatedBy());
    }

    private static class TestEntity extends AbstractEntity {
    }
}

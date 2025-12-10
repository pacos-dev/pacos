package org.pacos.core.config.worker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pacos.core.system.service.AutoUpdateService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

class UpdateCheckerWorkerTest {

    @Mock
    private AutoUpdateService autoUpdateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void whenCreateUpdateCheckerWorkerThenNoExceptionsThrown() {
        assertDoesNotThrow(() -> new UpdateCheckerWorker(autoUpdateService));
    }

    @Test
    void whenTriggerTaskThenDownloadNewVersionIfAvailable() {
        new UpdateCheckerWorker(autoUpdateService).runCheckTask();
        //then
        verify(autoUpdateService).updateSystem(false);
    }
}

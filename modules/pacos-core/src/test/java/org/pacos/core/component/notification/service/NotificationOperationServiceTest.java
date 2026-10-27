package org.pacos.core.component.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.core.component.notification.domain.NotificationOperation;
import org.pacos.core.component.notification.domain.NotificationOperationEvent;
import org.pacos.core.component.notification.domain.NotificationOperationStatus;
import org.pacos.core.component.notification.repository.NotificationOperationEventRepository;
import org.pacos.core.component.notification.repository.NotificationOperationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationOperationServiceTest {

    @Mock
    private NotificationOperationRepository operationRepository;

    @Mock
    private NotificationOperationEventRepository eventRepository;

    @InjectMocks
    private NotificationOperationService service;

    @Test
    void updateProgressRejectsValuesOutsideSupportedRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateProgress(7L, 101, "Downloading", null));
        verifyNoInteractions(operationRepository, eventRepository);
    }

    @Test
    void updateProgressStartsOperationAndRecordsStageTransition() {
        NotificationOperation operation = new NotificationOperation();
        operation.setId(7L);
        operation.setStatus(NotificationOperationStatus.PENDING);
        operation.setProgress(0);
        operation.setSourceKey("TEST");
        operation.setTitle("Test operation");
        when(operationRepository.findById(7L)).thenReturn(Optional.of(operation));
        when(operationRepository.save(any(NotificationOperation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationOperation updated = service.updateProgress(7L, 40, "Downloading", "Downloading artifact");

        assertEquals(NotificationOperationStatus.RUNNING, updated.getStatus());
        assertEquals(40, updated.getProgress());
        assertTrue(updated.getStartedAt() != null);
        verify(eventRepository).save(any(NotificationOperationEvent.class));
    }

    @Test
    void terminalOperationCannotBeUpdated() {
        NotificationOperation operation = new NotificationOperation();
        operation.setId(7L);
        operation.setStatus(NotificationOperationStatus.SUCCEEDED);
        when(operationRepository.findById(7L)).thenReturn(Optional.of(operation));

        assertThrows(IllegalStateException.class,
                () -> service.updateProgress(7L, 50, "Retry", null));
        verify(operationRepository, never()).save(any(NotificationOperation.class));
        verifyNoInteractions(eventRepository);
    }
}

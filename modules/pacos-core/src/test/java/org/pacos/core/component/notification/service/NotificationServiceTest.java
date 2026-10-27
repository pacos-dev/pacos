package org.pacos.core.component.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pacos.core.component.notification.domain.NotificationItemType;
import org.pacos.core.component.notification.domain.NotificationMessage;
import org.pacos.core.component.notification.domain.NotificationUserState;
import org.pacos.core.component.notification.repository.NotificationMessageRepository;
import org.pacos.core.component.notification.repository.NotificationOperationRepository;
import org.pacos.core.component.notification.repository.NotificationUserStateRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationMessageRepository messageRepository;

    @Mock
    private NotificationOperationRepository operationRepository;

    @Mock
    private NotificationUserStateRepository userStateRepository;

    @InjectMocks
    private NotificationService service;

    @Test
    void publishRejectsBlankTitleBeforeWritingToRepository() {
        NotificationMessage message = new NotificationMessage();
        message.setTitle("  ");
        message.setSourceKey("TEST");

        assertThrows(IllegalArgumentException.class, () -> service.publish(message));

        verifyNoInteractions(messageRepository, operationRepository, userStateRepository);
    }

    @Test
    void readStateIsSavedIndependentlyForEachUser() {
        when(messageRepository.existsById(41L)).thenReturn(true);
        when(userStateRepository.findByUserIdAndItemTypeAndItemId(anyInt(), any(), anyLong()))
                .thenReturn(Optional.empty());
        when(userStateRepository.save(any(NotificationUserState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.markRead(10, NotificationItemType.MESSAGE, 41L);
        service.markRead(11, NotificationItemType.MESSAGE, 41L);

        ArgumentCaptor<NotificationUserState> captor = ArgumentCaptor.forClass(NotificationUserState.class);
        verify(userStateRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(10, captor.getAllValues().get(0).getUserId());
        assertEquals(11, captor.getAllValues().get(1).getUserId());
        assertEquals(NotificationItemType.MESSAGE, captor.getAllValues().get(0).getItemType());
        assertEquals(41L, captor.getAllValues().get(0).getItemId());
        assertEquals(41L, captor.getAllValues().get(1).getItemId());
    }
}

package com.lab.notification.application;

import com.lab.notification.application.usecase.ProcessNotificationTaskUseCase;
import com.lab.notification.domain.model.*;
import com.lab.notification.domain.port.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class ProcessNotificationTaskUseCaseTest {

    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final ChannelPort emailChannel = mock(ChannelPort.class);
    private final ChannelPort smsChannel   = mock(ChannelPort.class);

    {
        when(emailChannel.supports(Channel.EMAIL)).thenReturn(true);
        when(emailChannel.supports(Channel.SMS)).thenReturn(false);
        when(smsChannel.supports(Channel.SMS)).thenReturn(true);
        when(smsChannel.supports(Channel.EMAIL)).thenReturn(false);
    }

    private final ProcessNotificationTaskUseCase useCase =
            new ProcessNotificationTaskUseCase(repository, List.of(emailChannel, smsChannel));

    @Test
    void sends_via_correct_channel_and_marks_sent() {
        NotificationTask task = new NotificationTask(
                UUID.randomUUID(), "user-1", Channel.EMAIL, Priority.CRITICAL, "OTP: 123");
        when(repository.existsSent(task.notificationId())).thenReturn(false);

        useCase.process(task);

        verify(emailChannel).send(task);
        verify(smsChannel, never()).send(any());
        verify(repository).updateStatus(task.notificationId(), NotificationStatus.SENT);
    }

    @Test
    void skips_already_sent_task() {
        NotificationTask task = new NotificationTask(
                UUID.randomUUID(), "user-1", Channel.EMAIL, Priority.CRITICAL, "OTP: 123");
        when(repository.existsSent(task.notificationId())).thenReturn(true);

        useCase.process(task);

        verifyNoInteractions(emailChannel, smsChannel);
        verify(repository, never()).updateStatus(any(), any());
    }
}

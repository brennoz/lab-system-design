package com.lab.notification.application;

import com.lab.notification.application.usecase.SendNotificationUseCase;
import com.lab.notification.domain.model.*;
import com.lab.notification.domain.port.*;
import com.lab.notification.domain.service.FanOutService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SendNotificationUseCaseTest {

    private final NotificationRepository repository  = mock(NotificationRepository.class);
    private final TaskQueuePort          taskQueue   = mock(TaskQueuePort.class);
    private final PreferencePort         preferences = mock(PreferencePort.class);
    private final FanOutService          fanOut      = new FanOutService();

    private final SendNotificationUseCase useCase =
            new SendNotificationUseCase(repository, taskQueue, preferences, fanOut);

    @Test
    void enqueues_task_for_each_active_recipient() {
        when(preferences.isOptedOut(any(), any())).thenReturn(false);

        useCase.send("OTP", List.of("user-1", "user-2"), Channel.EMAIL, Priority.CRITICAL, "code");

        verify(repository, times(2)).save(any());
        verify(taskQueue, times(2)).enqueue(any(), eq(Priority.CRITICAL));
    }

    @Test
    void skips_opted_out_recipient() {
        when(preferences.isOptedOut("user-1", Channel.SMS)).thenReturn(true);
        when(preferences.isOptedOut("user-2", Channel.SMS)).thenReturn(false);

        useCase.send("PROMO", List.of("user-1", "user-2"), Channel.SMS, Priority.BULK, "50% off");

        verify(taskQueue, times(1)).enqueue(any(), eq(Priority.BULK));
        verify(repository, times(1)).save(any());
    }

    @Test
    void does_not_enqueue_when_all_opted_out() {
        when(preferences.isOptedOut(any(), any())).thenReturn(true);

        useCase.send("PROMO", List.of("user-1"), Channel.EMAIL, Priority.BULK, "hi");

        verifyNoInteractions(taskQueue, repository);
    }
}

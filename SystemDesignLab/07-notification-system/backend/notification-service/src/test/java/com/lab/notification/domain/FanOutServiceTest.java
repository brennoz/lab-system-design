package com.lab.notification.domain;

import com.lab.notification.domain.model.*;
import com.lab.notification.domain.service.FanOutService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FanOutServiceTest {

    private final FanOutService fanOutService = new FanOutService();

    @Test
    void creates_one_task_per_recipient() {
        List<String> recipients = List.of("user-1", "user-2", "user-3");

        List<NotificationTask> tasks = fanOutService.fanOut(
                "OTP", recipients, Channel.EMAIL, Priority.CRITICAL, "Your OTP is 123456");

        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(NotificationTask::recipientId)
                .containsExactlyInAnyOrder("user-1", "user-2", "user-3");
    }

    @Test
    void each_task_has_correct_channel_priority_payload() {
        List<NotificationTask> tasks = fanOutService.fanOut(
                "PROMO", List.of("user-1"), Channel.SMS, Priority.BULK, "50% off!");

        NotificationTask task = tasks.get(0);
        assertThat(task.channel()).isEqualTo(Channel.SMS);
        assertThat(task.priority()).isEqualTo(Priority.BULK);
        assertThat(task.payload()).isEqualTo("50% off!");
    }

    @Test
    void each_task_has_unique_notification_id() {
        List<NotificationTask> tasks = fanOutService.fanOut(
                "OTP", List.of("user-1", "user-2"), Channel.EMAIL, Priority.CRITICAL, "code");

        assertThat(tasks.get(0).notificationId())
                .isNotEqualTo(tasks.get(1).notificationId());
    }
}

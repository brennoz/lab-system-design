package com.lab.notification.infrastructure.config;

import com.lab.notification.application.usecase.ProcessNotificationTaskUseCase;
import com.lab.notification.application.usecase.SendNotificationUseCase;
import com.lab.notification.domain.port.ChannelPort;
import com.lab.notification.domain.port.InboxPort;
import com.lab.notification.domain.port.NotificationRepository;
import com.lab.notification.domain.port.PreferencePort;
import com.lab.notification.domain.port.TaskQueuePort;
import com.lab.notification.infrastructure.inapp.InAppInboxAdapter;
import com.lab.notification.domain.service.FanOutService;
import com.lab.notification.infrastructure.channel.EmailChannelAdapter;
import com.lab.notification.infrastructure.channel.InAppChannelAdapter;
import com.lab.notification.infrastructure.channel.SmsChannelAdapter;
import com.lab.notification.infrastructure.inapp.SpringDataInAppRepository;
import com.lab.notification.infrastructure.persistence.JpaNotificationRepository;
import com.lab.notification.infrastructure.persistence.SpringDataNotificationRepository;
import com.lab.notification.infrastructure.preference.JpaPreferenceAdapter;
import com.lab.notification.infrastructure.preference.SpringDataPreferenceRepository;
import com.lab.notification.infrastructure.queue.RabbitMqTaskQueue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class AppConfig {

    @Value("${wiremock.base-url}")
    private String wiremockBaseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public NotificationRepository notificationRepository(SpringDataNotificationRepository delegate) {
        return new JpaNotificationRepository(delegate);
    }

    @Bean
    public PreferencePort preferencePort(SpringDataPreferenceRepository delegate) {
        return new JpaPreferenceAdapter(delegate);
    }

    @Bean
    public TaskQueuePort taskQueuePort(RabbitTemplate rabbitTemplate) {
        return new RabbitMqTaskQueue(rabbitTemplate);
    }

    @Bean
    public EmailChannelAdapter emailChannelAdapter(RestTemplate restTemplate) {
        return new EmailChannelAdapter(restTemplate, wiremockBaseUrl);
    }

    @Bean
    public SmsChannelAdapter smsChannelAdapter(RestTemplate restTemplate) {
        return new SmsChannelAdapter(restTemplate, wiremockBaseUrl);
    }

    @Bean
    public InAppChannelAdapter inAppChannelAdapter(SpringDataInAppRepository inAppRepository) {
        return new InAppChannelAdapter(inAppRepository);
    }

    @Bean
    public InboxPort inboxPort(SpringDataInAppRepository inAppRepository) {
        return new InAppInboxAdapter(inAppRepository);
    }

    @Bean
    public FanOutService fanOutService() {
        return new FanOutService();
    }

    @Bean
    public SendNotificationUseCase sendNotificationUseCase(
            NotificationRepository notificationRepository,
            TaskQueuePort taskQueuePort,
            PreferencePort preferencePort,
            FanOutService fanOutService) {
        return new SendNotificationUseCase(notificationRepository, taskQueuePort, preferencePort, fanOutService);
    }

    @Bean
    public ProcessNotificationTaskUseCase processNotificationTaskUseCase(
            NotificationRepository notificationRepository,
            List<ChannelPort> channelPorts) {
        return new ProcessNotificationTaskUseCase(notificationRepository, channelPorts);
    }
}

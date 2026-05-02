package com.lab.notification.infrastructure.channel;

import com.lab.notification.domain.model.Channel;
import com.lab.notification.domain.model.NotificationTask;
import com.lab.notification.domain.port.ChannelPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// Pattern: Strategy adapter — sends SMS via WireMock (simulates Twilio POST /v1/messages)
public class SmsChannelAdapter implements ChannelPort {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SmsChannelAdapter(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean supports(Channel channel) {
        return channel == Channel.SMS;
    }

    @Override
    public void send(NotificationTask task) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("to", task.recipientId(), "body", task.payload());
        restTemplate.postForObject(baseUrl + "/v1/messages", new HttpEntity<>(body, headers), String.class);
    }
}

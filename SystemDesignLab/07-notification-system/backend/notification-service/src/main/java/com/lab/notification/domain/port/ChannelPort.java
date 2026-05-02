package com.lab.notification.domain.port;

import com.lab.notification.domain.model.Channel;
import com.lab.notification.domain.model.NotificationTask;

// Pattern: Strategy port — one implementation per delivery channel
public interface ChannelPort {
    void send(NotificationTask task);
    boolean supports(Channel channel);
}

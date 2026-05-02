package com.lab.notification.api.dto;

import com.lab.notification.domain.model.Channel;

public record PreferenceRequest(Channel channel, boolean optedOut) {}

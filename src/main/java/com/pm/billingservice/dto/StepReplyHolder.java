package com.pm.billingservice.dto;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

/**
 * @author Achintha Kalunayaka
 * @since 10/24/2025
 */
@Component
public class StepReplyHolder {

    private MessageHeaders inboundHeaders;

    public void setInboundMessage(Message<?> message) {
        this.inboundHeaders = message.getHeaders();
    }

    public MessageHeaders getInboundHeaders() {
        return inboundHeaders;
    }
}
package com.infy.billing.dto.customer;

import com.infy.billing.entity.Notification.Channel;
import com.infy.billing.entity.Notification.Status;
import lombok.Data;

@Data
public class NotificationDTO {
    private Long id;
    private String type;
    private String subject;
    private String body;
    private Channel channel;
    private Status status;
    private String createdAt;
}

package com.infy.billing.dto.customer;

import java.time.LocalDateTime;

import com.infy.billing.enums.Channel;
import com.infy.billing.enums.Status;

import lombok.Data;

@Data
public class NotificationDTO {
   private Long notificationId;
   private String type;
   private String subject;
   private String body;
   private Channel channel;
   private Status status;
   private LocalDateTime scheduledAt;
   private LocalDateTime sentAt;
   private LocalDateTime createdAt;
   private Boolean isRead;
}

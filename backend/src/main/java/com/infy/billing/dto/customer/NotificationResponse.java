package com.infy.billing.dto.customer;
import java.time.LocalDateTime;
import com.infy.billing.enums.Channel;
import com.infy.billing.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class NotificationResponse {

	private Long notificationId;

	private String type;

	private String subject;

	private String body;

	private Channel channel;

	private NotificationStatus status;

	private LocalDateTime scheduledAt;

	private LocalDateTime sentAt;

	private LocalDateTime createdAt;
}

package com.infy.billing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.infy.billing.enums.Channel;
import com.infy.billing.enums.Status;

@Entity
@Table(name = "notification")
@Data
public class Notification {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "notification_id")
private Long id;

@ManyToOne
@JoinColumn(name = "customer_id")
private Customer customer;

private String type;
private String subject;
private String body;

@Enumerated(EnumType.STRING)
private Channel channel;

@Enumerated(EnumType.STRING)
private Status status;

private LocalDateTime scheduledAt;
private LocalDateTime sentAt;

@Column(nullable = false)
private LocalDateTime createdAt;

@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
}

}

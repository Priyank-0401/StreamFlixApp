package com.infy.billing.service;

import com.infy.billing.dto.customer.NotificationDTO;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.Notification;
import com.infy.billing.entity.User;
import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.NotificationRepository;
import com.infy.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public List<NotificationDTO> getCustomerNotifications(String email) {
        Customer customer = getCustomerByEmail(email);
        return notificationRepository.findByCustomer_IdOrderByCreatedAtDesc(customer.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(String email) {
        Customer customer = getCustomerByEmail(email);
        return notificationRepository.findByCustomer_IdAndStatusOrderByCreatedAtDesc(customer.getId(), Notification.Status.PENDING)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(String email, Long notificationId) {
        Customer customer = getCustomerByEmail(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        notification.setStatus(Notification.Status.READ);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        Customer customer = getCustomerByEmail(email);
        List<Notification> unread = notificationRepository.findByCustomer_IdAndStatusOrderByCreatedAtDesc(customer.getId(), Notification.Status.PENDING);
        unread.forEach(n -> n.setStatus(Notification.Status.READ));
        notificationRepository.saveAll(unread);
    }

    private Customer getCustomerByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return customerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    private NotificationDTO mapToDTO(Notification entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setSubject(entity.getSubject());
        dto.setBody(entity.getBody());
        dto.setChannel(entity.getChannel());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt().toString());
        return dto;
    }
}

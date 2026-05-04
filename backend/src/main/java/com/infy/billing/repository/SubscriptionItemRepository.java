package com.infy.billing.repository;

import com.infy.billing.entity.SubscriptionItem;
import com.infy.billing.enums.ItemType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionItemRepository extends JpaRepository<SubscriptionItem, Long> {
	List<SubscriptionItem> findBySubscription_Id(Long subscriptionId);
	SubscriptionItem findBySubscription_IdAndItemType(Long subscriptionId, ItemType plan);
	SubscriptionItem findBySubscription_IdAndAddOn_Id(Long subscriptionId, Long addonId);
}

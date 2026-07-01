package com.atamanahmet.vinylexchange.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.repository.order.OrderRepository;

@Component
public class KvkkRetentionScheduler {

    private final OrderRepository orderRepository;
    private final Logger logger;

    public KvkkRetentionScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.logger = LoggerFactory.getLogger(KvkkRetentionScheduler.class);
    }

    /** Nullifies PII snapshots on completed or refunded orders older than 24 months per KVKK retention rules. */
    @Scheduled(cron = "0 0 3 1 * *")
    @Transactional
    public void purgeExpiredAddressSnapshots() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(24);
        List<OrderStatus> statuses = List.of(OrderStatus.COMPLETED, OrderStatus.REFUNDED);
        List<UUID> ids = orderRepository.findIdsForKvkkPurge(statuses, cutoff);

        if (!ids.isEmpty()) {
            orderRepository.nullifyAddressSnapshots(ids);
        }

        logger.info("KVKK purge complete. {} orders cleared.", ids.size());
    }
}

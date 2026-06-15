package com.atamanahmet.vinylexchange.service.payment;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.event.OrderShippedEvent;
import com.atamanahmet.vinylexchange.exception.InvalidStatusTransitionException;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoConfirmSchedulerServiceTest {

    @Mock private TaskScheduler taskScheduler;
    @Mock private OrderService orderService;

    @InjectMocks
    private AutoConfirmSchedulerService autoConfirmSchedulerService;

    private UUID orderId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        order = Order.builder()
                .id(orderId)
                .status(OrderStatus.SHIPPED)
                .autoConfirmDeadline(LocalDateTime.now().plusDays(3))
                .build();
    }

    /**
     * Order shipped, scheduler must be called with correct deadline instant
     */
    @Test
    void onOrderShipped_withDeadline_schedulesTaskAtCorrectTime() {
        when(orderService.getOrderById(orderId)).thenReturn(order);

        autoConfirmSchedulerService.onOrderShipped(new OrderShippedEvent(orderId));

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(taskScheduler).schedule(any(Runnable.class), instantCaptor.capture());

        Instant expectedInstant = order.getAutoConfirmDeadline().toInstant(ZoneOffset.UTC);
        assertThat(instantCaptor.getValue()).isEqualTo(expectedInstant);
    }

    /**
     * Order has no autoConfirmDeadline set
     */
    @Test
    void onOrderShipped_nullDeadline_skipsScheduling() {
        order.setAutoConfirmDeadline(null);
        when(orderService.getOrderById(orderId)).thenReturn(order);

        autoConfirmSchedulerService.onOrderShipped(new OrderShippedEvent(orderId));

        verifyNoInteractions(taskScheduler);
    }

    /**
     * Scheduled task fires, autoConfirmDelivery must be called for that order
     */
    @Test
    void autoConfirm_callsAutoConfirmDelivery() {
        when(orderService.getOrderById(orderId)).thenReturn(order);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        autoConfirmSchedulerService.onOrderShipped(new OrderShippedEvent(orderId));
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));

        runnableCaptor.getValue().run();

        verify(orderService).autoConfirmDelivery(orderId);
    }

    /**
     * Scheduled task fires but order already delivered by buyer, exception must be swallowed
     * autoConfirmDelivery throws because status is not SHIPPED anymore
     */
    @Test
    void autoConfirm_exceptionFromOrderService_doesNotPropagate() {
        when(orderService.getOrderById(orderId)).thenReturn(order);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        autoConfirmSchedulerService.onOrderShipped(new OrderShippedEvent(orderId));
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));

        doThrow(new InvalidStatusTransitionException("already delivered"))
                .when(orderService).autoConfirmDelivery(orderId);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> runnableCaptor.getValue().run()
        );
    }
}
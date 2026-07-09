package com.atamanahmet.vinylexchange.controller.order;

import com.atamanahmet.vinylexchange.dto.shipment.GenerateLabelRequest;
import com.atamanahmet.vinylexchange.dto.order.CancelRequest;
import com.atamanahmet.vinylexchange.dto.payment.DisputeRequest;
import com.atamanahmet.vinylexchange.dto.payment.DisputeResolveRequest;
import com.atamanahmet.vinylexchange.dto.order.OrderDTO;
import com.atamanahmet.vinylexchange.service.order.CancelService;
import com.atamanahmet.vinylexchange.service.order.OrderAccessService;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CancelService cancelService;
    private final OrderAccessService orderAccessService;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable UUID orderId) {
        orderAccessService.assertCanView(orderId, UserUtil.getCurrentUserId());
        return ResponseEntity.ok(orderService.getOrderDto(orderId, UserUtil.getCurrentUserId()));
    }

    @GetMapping("/my/purchases")
    public ResponseEntity<List<OrderDTO>> getMyPurchases() {
        return ResponseEntity.ok(orderService.getOrderDtosByBuyerId(UserUtil.getCurrentUserId()));
    }

    @GetMapping("/my/sales")
    public ResponseEntity<List<OrderDTO>> getMySales() {
        return ResponseEntity.ok(orderService.getOrderDtosBySellerId(UserUtil.getCurrentUserId()));
    }

    @PostMapping("/{orderId}/shipment/label")
    public ResponseEntity<OrderDTO> generateShipmentLabel(
            @PathVariable UUID orderId,
            @RequestBody @Valid GenerateLabelRequest request) {
        orderService.generateShipmentLabel(
                orderId,
                UserUtil.getCurrentUserId(),
                request.getHandlerCode(),
                request.getSellerAddressId());
        return ResponseEntity.ok(orderService.getOrderDto(orderId, UserUtil.getCurrentUserId()));
    }

    @PostMapping("/{orderId}/confirm-delivery")
    public ResponseEntity<OrderDTO> confirmDelivery(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.confirmDelivery(orderId, UserUtil.getCurrentUserId()));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody @Valid CancelRequest request) {
        cancelService.cancelOrder(orderId, UserUtil.getCurrentUserId(), request.reason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/dispute")
    public ResponseEntity<Void> openDispute(
            @PathVariable UUID orderId,
            @RequestBody @Valid DisputeRequest request) {
        cancelService.openDispute(orderId, UserUtil.getCurrentUserId(),
                request.reason(), request.note());
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin only
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{orderId}/dispute/resolve")
    public ResponseEntity<Void> resolveDispute(
            @PathVariable UUID orderId,
            @RequestBody @Valid DisputeResolveRequest request) {
        cancelService.resolveDispute(orderId, UserUtil.getCurrentUserId(),
                request.resolution(), request.reviewNote());
        return ResponseEntity.noContent().build();
    }
}

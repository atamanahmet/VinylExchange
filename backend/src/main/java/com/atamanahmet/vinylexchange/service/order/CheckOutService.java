package com.atamanahmet.vinylexchange.service.order;

import com.atamanahmet.vinylexchange.domain.entity.Cart;
import com.atamanahmet.vinylexchange.domain.entity.CartItem;
import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.domain.entity.OrderItem;
import com.atamanahmet.vinylexchange.domain.enums.ErrorType;
import com.atamanahmet.vinylexchange.domain.enums.IssueType;
import com.atamanahmet.vinylexchange.domain.enums.OrderStatus;
import com.atamanahmet.vinylexchange.domain.enums.SaleType;
import com.atamanahmet.vinylexchange.domain.snapshot.AddressSnapshot;
import com.atamanahmet.vinylexchange.dto.order.CartValidationIssue;
import com.atamanahmet.vinylexchange.dto.order.CheckoutResponseDTO;
import com.atamanahmet.vinylexchange.event.OrderCreatedEvent;
import com.atamanahmet.vinylexchange.exception.CheckOutProcessingException;
import com.atamanahmet.vinylexchange.exception.CheckOutValidationException;
import com.atamanahmet.vinylexchange.mapper.OrderMapper;
import com.atamanahmet.vinylexchange.service.listing.ListingService;
import com.atamanahmet.vinylexchange.service.user.UserAddressService;
import com.atamanahmet.vinylexchange.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckOutService {

    private final CartService cartService;
    private final ListingService listingService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final UserAddressService userAddressService;

    @Transactional
    public CheckoutResponseDTO proceedCheckOut(UUID userId, UUID shippingAddressId) {
        // Buyer address snapshot is shared across all orders created in this checkout
        AddressSnapshot addressSnapshot = userAddressService.toSnapshot(
                userAddressService.getAddressOrThrow(userId, shippingAddressId));

        Cart cart = cartService.getCart(userId);

        List<UUID> listingIds = cart.getCartItems().stream()
                .map(CartItem::getListingId)
                .collect(Collectors.toList());

        Map<UUID, Listing> listingMap = listingService.getListingsByIdsWithLock(listingIds)
                .stream()
                .collect(Collectors.toMap(Listing::getId, l -> l));

        List<CartValidationIssue> issues = validateCartItems(cart.getCartItems(), listingMap);

        if (hasErrors(issues)) {
            throw new CheckOutValidationException(issues);
        }

        try {
            List<Order> orders = createOrdersPerSeller(
                    userId, cart.getCartItems(), listingMap, addressSnapshot);
            cartService.clearCart(userId);
            Map<UUID, String> listingPublicIds = listingMap.values().stream()
                    .collect(Collectors.toMap(Listing::getId, Listing::getPublicId));
            Map<UUID, String> usernames = userService.findUsernamesByIds(
                    orders.stream()
                            .flatMap(o -> java.util.stream.Stream.of(o.getBuyerId(), o.getSellerId()))
                            .distinct()
                            .toList());
            return OrderMapper.toCheckoutResponse(
                    orders, listingPublicIds, usernames, userId, addressSnapshot);
        } catch (Exception e) {
            log.error("Checkout failed for userId={}", userId, e);
            throw new CheckOutProcessingException();
        }
    }

    /**
     * Groups cart items by seller and creates one order per seller
     */
    private List<Order> createOrdersPerSeller(
            UUID buyerId,
            List<CartItem> cartItems,
            Map<UUID, Listing> listingMap,
            AddressSnapshot addressSnapshot) {

        Map<UUID, List<CartItem>> itemsBySeller = cartItems.stream()
                .collect(Collectors.groupingBy(
                        item -> listingMap.get(item.getListingId()).getOwnerId()));

        List<Order> createdOrders = new ArrayList<>();
        List<Listing> listingsToUpdate = new ArrayList<>();

        for (Map.Entry<UUID, List<CartItem>> entry : itemsBySeller.entrySet()) {

            UUID sellerId = entry.getKey();
            List<CartItem> sellerItems = entry.getValue();

            Order order = buildOrder(
                    buyerId, sellerId, sellerItems, listingMap, listingsToUpdate, addressSnapshot);

            createdOrders.add(order);

            eventPublisher.publishEvent(new OrderCreatedEvent(
                    order.getId(),
                    buyerId,
                    sellerId,
                    order.getSaleType(),
                    order.getTotalPrice()));

            log.info("Order created orderNumber={} seller={} buyer={}",
                    order.getOrderNumber(), sellerId, buyerId);
        }

        listingService.saveAllListing(listingsToUpdate);

        return createdOrders;
    }

    /**
     * Builds and persists one order with its items for a single seller
     */
    private Order buildOrder(
            UUID buyerId,
            UUID sellerId,
            List<CartItem> sellerItems,
            Map<UUID, Listing> listingMap,
            List<Listing> listingsToUpdate,
            AddressSnapshot addressSnapshot) {

        SaleType saleType = sellerItems.stream()
                .map(item -> listingMap.get(item.getListingId()).getSaleType())
                .anyMatch(t -> t == SaleType.FIXED_PRICE)
                        ? SaleType.FIXED_PRICE
                        : SaleType.TRADE;

        Order order = Order.builder()
                .orderNumber(orderService.getNextOrderNumber())
                .buyerId(buyerId)
                .sellerId(sellerId)
                .saleType(saleType)
                .status(OrderStatus.AWAITING_PAYMENT)
                .orderItems(new ArrayList<>())
                .build();

        order = orderService.saveOrder(order);

        long totalPrice = 0L;

        for (CartItem item : sellerItems) {

            Listing listing = listingMap.get(item.getListingId());

            String mainImagePath = listing.getMainImageUrl();

            OrderItem orderItem = orderItemService.saveOrderItemAndFlush(
                    OrderItem.builder()
                            .order(order)
                            .listingId(listing.getId())
                            .listingTitle(listing.getTitle())
                            .listingMainImageUrl(mainImagePath)
                            .sellerId(sellerId)
                            .unitPrice(listing.getPriceKurus())
                            .quantity(item.getOrderQuantity())
                            .subTotal(listing.getPriceKurus() * item.getOrderQuantity())
                            .build());

            order.getOrderItems().add(orderItem);
            totalPrice += orderItem.getSubTotal();

            listing.setStockQuantity(listing.getStockQuantity() - item.getOrderQuantity());
            listingsToUpdate.add(listing);
        }

        order.setTotalPrice(totalPrice);
        order.setShippingDeadline(LocalDateTime.now().plusDays(5));
        order.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(15));
        order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(7));
        order.setShippingAddressSnapshot(addressSnapshot);
        order.setBillingAddressSnapshot(addressSnapshot);

        return orderService.saveOrder(order);
    }

    private List<CartValidationIssue> validateCartItems(
            List<CartItem> cartItems,
            Map<UUID, Listing> listingMap) {

        List<CartValidationIssue> issues = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Listing listing = listingMap.get(cartItem.getListingId());

            if (listing == null) {
                issues.add(CartValidationIssue.builder()
                        .cartItemId(cartItem.getCartItemId())
                        .publicId(listing != null ? listing.getPublicId() : null)
                        .type(IssueType.LISTING_DELETED)
                        .message("Listing is no longer available")
                        .errorType(ErrorType.ERROR)
                        .build());
                continue;
            }

            if (!listing.isAvailable()) {
                issues.add(CartValidationIssue.builder()
                        .cartItemId(cartItem.getCartItemId())
                        .publicId(listing != null ? listing.getPublicId() : null)
                        .type(IssueType.SOLD_OUT)
                        .message(listing.getTitle() + " is no longer available")
                        .errorType(ErrorType.ERROR)
                        .build());
                continue;
            }

            if (!listing.hasEnoughStock(cartItem.getOrderQuantity())) {
                issues.add(CartValidationIssue.builder()
                        .cartItemId(cartItem.getCartItemId())
                        .publicId(listing != null ? listing.getPublicId() : null)
                        .type(IssueType.INSUFFICIENT_STOCK)
                        .message("Not enough stock for " + listing.getTitle())
                        .errorType(ErrorType.ERROR)
                        .build());
            }
        }

        return issues;
    }

    private boolean hasErrors(List<CartValidationIssue> issues) {
        return issues.stream().anyMatch(i -> i.errorType() == ErrorType.ERROR);
    }
}
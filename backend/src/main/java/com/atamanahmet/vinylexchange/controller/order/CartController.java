package com.atamanahmet.vinylexchange.controller.order;

import com.atamanahmet.vinylexchange.dto.order.AddToCartRequest;
import com.atamanahmet.vinylexchange.dto.order.CartDTO;
import com.atamanahmet.vinylexchange.dto.order.CheckoutResponseDTO;
import com.atamanahmet.vinylexchange.dto.order.UpdateCartItemRequest;
import com.atamanahmet.vinylexchange.mapper.OrderMapper;
import com.atamanahmet.vinylexchange.service.order.CartService;
import com.atamanahmet.vinylexchange.service.order.CheckOutService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

        private final CartService cartService;
        private final CheckOutService checkOutService;

        @GetMapping
        public ResponseEntity<CartDTO> getCart() {
                return ResponseEntity.ok(cartService.getCartDTO(UserUtil.getCurrentUserId()));
        }

        @PostMapping("/items")
        public ResponseEntity<CartDTO> addToCart(@RequestBody @Valid AddToCartRequest request) {
                return ResponseEntity.ok(cartService.addToCart(
                        UserUtil.getCurrentUserId(),
                        request.listingId(),
                        request.quantity()));
        }

        /**
         * Decrease item quantity by 1, removes item if quantity reaches 0
         */
        @PatchMapping("/items/{listingId}")
        public ResponseEntity<CartDTO> decreaseItemQuantity(@PathVariable UUID listingId) {
                return ResponseEntity
                        .ok(cartService.decreaseItemQuantity(
                        UserUtil.getCurrentUserId(),
                        listingId));
        }

        /**
         * Set exact quantity for a cart item
         */
        @PutMapping("/items/{listingId}/quantity")
        public ResponseEntity<CartDTO> updateItemQuantity(
                @PathVariable UUID listingId,
                @RequestBody @Valid UpdateCartItemRequest request) {
                return ResponseEntity.ok(cartService.updateCartItemQuantity(
                        UserUtil.getCurrentUserId(),
                        listingId,
                        request));
        }

        @DeleteMapping("/items/{cartItemId}")
        public ResponseEntity<Void> removeFromCart(@PathVariable UUID cartItemId) {
                cartService.removeItemFromCart(UserUtil.getCurrentUserId(), cartItemId);
                return ResponseEntity.noContent().build();
        }

        @DeleteMapping
        public ResponseEntity<Void> clearCart() {
                cartService.clearCart(UserUtil.getCurrentUserId());
                return ResponseEntity.noContent().build();
        }

        /**
         * Submits cart as orders, one order per seller
         */
        @PostMapping("/checkout")
        public ResponseEntity<CheckoutResponseDTO> checkout() {
                CheckoutResponseDTO response = OrderMapper.toCheckoutResponse(
                        checkOutService.proceedCheckOut(UserUtil.getCurrentUserId()));
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
}
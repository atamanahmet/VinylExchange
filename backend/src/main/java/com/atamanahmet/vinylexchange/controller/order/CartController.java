package com.atamanahmet.vinylexchange.controller.order;

import java.util.UUID;

import com.atamanahmet.vinylexchange.service.order.CartService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atamanahmet.vinylexchange.dto.order.AddToCartRequest;
import com.atamanahmet.vinylexchange.dto.order.CartDTO;

@RestController
@RequestMapping("/api/cart")
public class CartController {

        private final CartService cartService;

        public CartController(CartService cartService) {

                this.cartService = cartService;
        }

        @GetMapping
        public ResponseEntity<CartDTO> getCart() {

                CartDTO cartDTO = cartService.getCartDTO(UserUtil.getCurrentUserId());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(cartDTO);
        }

        @PostMapping("/items")
        public ResponseEntity<CartDTO> addToCart(
                        @RequestBody AddToCartRequest requestDTO) {

                CartDTO cartDTO = cartService.addToCart(
                                UserUtil.getCurrentUserId(), // UUID
                                requestDTO.listingId(), // UUID
                                requestDTO.quantity()); // int

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(cartDTO);
        }

        @DeleteMapping("/items/{cartItemId}")
        public ResponseEntity<CartDTO> removeFromCart(
                @PathVariable(name = "cartItemId", required = true) UUID cartItemId) {

                CartDTO cartDTO = cartService.removeItemFromCart(
                                UserUtil.getCurrentUserId(),
                                cartItemId);

                return ResponseEntity
                                .status(HttpStatus.NO_CONTENT)
                                .body(cartDTO);
        }

        @PatchMapping("/items/{cartItemId}")
        public ResponseEntity<HttpStatus> updateCart(
                        @PathVariable(name = "cartItemId", required = true) UUID cartItemId) {

                cartService.decreaseItemQuantity(
                                UserUtil.getCurrentUserId(),
                                cartItemId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .build();

        }

        @DeleteMapping
        public ResponseEntity<HttpStatus> clearCart() {

                cartService.clearCart(UserUtil.getCurrentUserId());

                return ResponseEntity
                                .noContent()
                                .build();
        }
}

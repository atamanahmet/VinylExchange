package com.atamanahmet.vinylexchange.controller.order;

import java.util.List;

import com.atamanahmet.vinylexchange.domain.entity.Order;
import com.atamanahmet.vinylexchange.service.order.OrderService;
import com.atamanahmet.vinylexchange.session.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getOrders()
            throws JsonProcessingException {

        List<Order> orders = orderService.getAllOrdersByUserId(UserUtil.getCurrentUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orders);
    }

}

package com.atamanahmet.vinylexchange.controller.payment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.atamanahmet.vinylexchange.domain.enums.PaymentStatus;
import com.atamanahmet.vinylexchange.dto.payment.RefundReviewPaymentDto;
import com.atamanahmet.vinylexchange.service.payment.PaymentService;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = AdminPaymentControllerTest.TestConfig.class)
class AdminPaymentControllerTest {

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        PaymentService paymentService() {
            return mock(PaymentService.class);
        }

        @Bean
        AdminPaymentController adminPaymentController(PaymentService paymentService) {
            return new AdminPaymentController(paymentService);
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PaymentService paymentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listRefundReviewPayments_asAdmin_returns200WithExpectedData() throws Exception {
        UUID orderId = UUID.fromString("a1000001-0001-4001-8001-000000000001");
        UUID paymentId = UUID.fromString("b2000001-0001-4001-8001-000000000001");
        LocalDateTime capturedAt = LocalDateTime.of(2026, 6, 20, 12, 0);

        when(paymentService.listRefundReviewRequired()).thenReturn(List.of(
                new RefundReviewPaymentDto(
                        orderId,
                        paymentId,
                        150000L,
                        capturedAt,
                        PaymentStatus.REFUNDED)));

        mockMvc.perform(get("/api/admin/payments/refund-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$[0].paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$[0].amount").value(150000))
                .andExpect(jsonPath("$[0].status").value("REFUNDED"));
    }

    @Test
    void listRefundReviewPayments_withoutAdminRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/payments/refund-review"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/payments/refund-review")
                        .with(user("buyer").roles("USER")))
                .andExpect(status().isForbidden());
    }
}

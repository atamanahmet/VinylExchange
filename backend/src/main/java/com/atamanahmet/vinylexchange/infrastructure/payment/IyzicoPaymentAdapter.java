package com.atamanahmet.vinylexchange.infrastructure.payment;

import com.atamanahmet.vinylexchange.domain.entity.PaymentTransaction;
import com.atamanahmet.vinylexchange.dto.payment.PaymentInitiateResponse;
import com.atamanahmet.vinylexchange.dto.payment.PaymentVerifyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "iyzico")
@RequiredArgsConstructor
public class IyzicoPaymentAdapter implements PaymentPort {


    private final WebClient webClient;
    private final IyzicoProperties props;

    @Override
    public PaymentInitiateResponse initiateCheckoutForm(PaymentTransaction payment, String callbackUrl) {
        try {
            String conversationId = payment.getId().toString();
            String randomString = generateRandomString();
            String requestBody = buildInitiateRequestBody(payment, callbackUrl, conversationId);
            log.info("[IYZICO] Request body: {}", requestBody);
            String authHeader = buildAuthHeader(randomString, requestBody,
                    "/payment/iyzipos/checkoutform/initialize/auth/ecom");

            Map<String, Object> response = webClient.post()
                    .uri(props.getBaseUrl() + "/payment/iyzipos/checkoutform/initialize/auth/ecom")
                    .header("Authorization", authHeader)
                    .header("x-iyzi-rnd", randomString)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !"success".equals(response.get("status"))) {
                String errorMsg = response != null ? (String) response.get("errorMessage") : "null response";
                log.error("[IYZICO] Checkout form init failed: {}", errorMsg);
                return PaymentInitiateResponse.failure(errorMsg);
            }

            String token = (String) response.get("token");
            String formContent = (String) response.get("checkoutFormContent");

            log.info("[IYZICO] Checkout form created payment={} token={}", payment.getId(), token);
            return PaymentInitiateResponse.success(token, formContent);

        } catch (Exception e) {
            log.error("[IYZICO] initiateCheckoutForm failed payment={}", payment.getId(), e);
            return PaymentInitiateResponse.failure("Payment initiation error: " + e.getMessage());
        }
    }

    @Override
    public PaymentVerifyResult verifyCallback(String token) {
        try {
            String randomString = generateRandomString();
            String requestBody = "{\"locale\":\"tr\",\"token\":\"" + token + "\"}";
            String authHeader = buildAuthHeader(randomString, requestBody,
                    "/payment/iyzipos/checkoutform/auth/ecom/detail");

            Map<String, Object> response = webClient.post()
                    .uri(props.getBaseUrl() + "/payment/iyzipos/checkoutform/auth/ecom/detail")
                    .header("Authorization", authHeader)
                    .header("x-iyzi-rnd", randomString)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            log.info("[IYZICO] Verify response: {}", response);

            if (response == null) return PaymentVerifyResult.failure();

            String paymentStatus = (String) response.get("paymentStatus");
            boolean success = "success".equals(response.get("status"))
                    && "SUCCESS".equals(paymentStatus);

            return new PaymentVerifyResult(
                    success,
                    paymentStatus,
                    String.valueOf(response.get("paymentId")),
                    (String) response.get("authCode"),
                    (String) response.get("hostReference"),
                    response.get("fraudStatus") != null
                            ? ((Number) response.get("fraudStatus")).intValue()
                            : null
            );

        } catch (Exception e) {
            log.error("[IYZICO] verifyCallback failed token={}", token, e);
            return PaymentVerifyResult.failure();
        }
    }

    @Override
    public String approvePayoutToSeller(PaymentTransaction payment) {
        /**
         * Iyzico escrow payout — approve sub-merchant payment
         * Implement when sub-merchant (seller) onboarding is ready
         */
        log.warn("[IYZICO] approvePayoutToSeller not yet implemented, payment={}", payment.getId());
        return "IYZICO-PAYOUT-PENDING";
    }

    @Override
    public void refundToBuyer(PaymentTransaction payment) {
        /**
         * Iyzico refund endpoint
         * Implement when refund flow is tested on sandbox
         */
        log.warn("[IYZICO] refundToBuyer not yet implemented, payment={}", payment.getId());
    }

    /**
     * Iyzico HMAC-SHA256 auth header
     *
     * Iyzico IYZWSv2 auth formula:
     * payload = randomKey + uriPath + requestBody
     * encryptedData = HmacSHA256(payload, secretKey) as hex
     * authorizationString = "apiKey:" + apiKey + "&randomKey:" + randomKey + "&signature:" + encryptedData
     * header = "IYZWSv2 " + base64(authorizationString)
     */
    private String buildAuthHeader(String randomString, String requestBody, String uriPath) throws Exception {

        String payload = randomString + uriPath + requestBody;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(props.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexSignature = new StringBuilder();
        for (byte b : hash) {
            hexSignature.append(String.format("%02x", b));
        }

        String authorizationString = "apiKey:" + props.getApiKey()
                + "&randomKey:" + randomString
                + "&signature:" + hexSignature;

        String base64Encoded = Base64.getEncoder()
                .encodeToString(authorizationString.getBytes(StandardCharsets.UTF_8));

        return "IYZWSv2 " + base64Encoded;
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * Build Iyzico checkout form init request
     * buyer/shipping/billing address fields needed for full integration
     * minimal version for sandbox testing
     */
    private String buildInitiateRequestBody(PaymentTransaction payment, String callbackUrl, String conversationId) {
        return """
                {
                    "locale": "tr",
                    "conversationId": "%s",
                    "price": "%s",
                    "paidPrice": "%s",
                    "currency": "TRY",
                    "basketId": "%s",
                    "paymentGroup": "PRODUCT",
                    "callbackUrl": "%s",
                    "enabledInstallments": [1, 2, 3, 6],
                    "buyer": {
                        "id": "%s",
                        "name": "Buyer",
                        "surname": "User",
                        "email": "buyer@vinylexchange.com",
                        "gsmNumber": "+905555555555",
                        "identityNumber": "11111111111",
                        "registrationAddress": "Istanbul",
                        "city": "Istanbul",
                        "country": "Turkey",
                        "ip": "85.34.78.112"
                    },
                    "shippingAddress": {
                        "contactName": "Buyer User",
                        "city": "Istanbul",
                        "country": "Turkey",
                        "address": "Istanbul"
                    },
                    "billingAddress": {
                        "contactName": "Buyer User",
                        "city": "Istanbul",
                        "country": "Turkey",
                        "address": "Istanbul"
                    },
                    "basketItems": [%s]
                }
                """.formatted(
                conversationId,
                formatPrice(payment.getAmountKurus()),
                formatPrice(payment.getAmountKurus()),
                payment.getOrder().getId(),
                callbackUrl,
                payment.getOrder().getBuyerId(),
                buildBasketItems(payment)
        );
    }

    private String buildBasketItems(PaymentTransaction payment) {
        return payment.getOrder().getOrderItems().stream()
                .map(item -> """
                        {
                            "id": "%s",
                            "name": "%s",
                            "category1": "Vinyl Record",
                            "itemType": "PHYSICAL",
                            "price": "%s"
                        }
                        """.formatted(
                        item.getListingId(),
                        item.getListingTitle(),
                        formatPrice(item.getSubTotal())
                ))
                .collect(Collectors.joining(","));
    }

    /**
     * Iyzico expects price as decimal string, we store as kurus (cents)
     * 1000 kurus = 10.00 TRY
     */
    private String formatPrice(long kurus) {
        return String.format("%.2f", kurus / 100.0);
    }
}
package com.atamanahmet.vinylexchange.infrastructure.payment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment.iyzico")
public class IyzicoProperties {
    private String apiKey;
    private String secretKey;
    private String baseUrl;
    private String callbackUrl;
}
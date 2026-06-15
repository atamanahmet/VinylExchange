package com.atamanahmet.vinylexchange.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds platform.fee.* from application.properties
 * feeBP: basis points, 1000 = 10%, 500 = 5%
 * Change per category in future when Category entity exists
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "platform.fee")
public class PlatformFeeProperties {

    /**
     * Platform cut in basis points
     * 1000 = 10%
     */
    private int bp = 1000;
}
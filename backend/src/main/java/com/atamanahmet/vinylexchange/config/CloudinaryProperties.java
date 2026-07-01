package com.atamanahmet.vinylexchange.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    private String url;
}

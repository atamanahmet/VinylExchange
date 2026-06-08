package com.atamanahmet.vinylexchange.infrastructure;

import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import lombok.Value;

@Value
public class ImageUploadResult {
    String publicId;
    String secureUrl;
    int position;
    StorageProvider provider;
}
package com.atamanahmet.vinylexchange.dto;

import com.atamanahmet.vinylexchange.dto.listing.AddToWishlistRequest;
import com.atamanahmet.vinylexchange.dto.user.TradePreferenceRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecordDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void addToWishlistRequest_rejectsBlankTitle() {
        AddToWishlistRequest req = new AddToWishlistRequest("", "Artist", null, null, null, null, null, null);
        assertThat(validator.validate(req))
                .anyMatch(v -> "title".equals(v.getPropertyPath().toString()));
    }

    @Test
    void addToWishlistRequest_rejectsBlankArtist() {
        AddToWishlistRequest req = new AddToWishlistRequest("Title", "", null, null, null, null, null, null);
        assertThat(validator.validate(req))
                .anyMatch(v -> "artist".equals(v.getPropertyPath().toString()));
    }

    @Test
    void addToWishlistRequest_validPasses() {
        AddToWishlistRequest req = new AddToWishlistRequest("Title", "Artist", null, null, null, null, null, null);
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void tradePreferenceRequest_rejectsBlankDesiredItem() {
        TradePreferenceRequest req = new TradePreferenceRequest("  ", 0.0, null);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void tradePreferenceRequest_rejectsOversizedDesiredItem() {
        TradePreferenceRequest req = new TradePreferenceRequest("x".repeat(201), 0.0, null);
        assertThat(validator.validate(req))
                .anyMatch(v -> "desiredItem".equals(v.getPropertyPath().toString()));
    }

    @Test
    void tradePreferenceRequest_validPasses() {
        TradePreferenceRequest req = new TradePreferenceRequest("Dark Side of the Moon", 0.0, null);
        assertThat(validator.validate(req)).isEmpty();
    }
}

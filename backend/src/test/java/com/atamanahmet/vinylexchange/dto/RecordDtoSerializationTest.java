package com.atamanahmet.vinylexchange.dto;

import com.atamanahmet.vinylexchange.domain.PaymentDirection;
import com.atamanahmet.vinylexchange.domain.entity.TradePreference;
import com.atamanahmet.vinylexchange.domain.enums.IssueType;
import com.atamanahmet.vinylexchange.dto.listing.AddToWishlistBulkRequest;
import com.atamanahmet.vinylexchange.dto.listing.AddToWishlistFailureDTO;
import com.atamanahmet.vinylexchange.dto.listing.AddToWishlistRequest;
import com.atamanahmet.vinylexchange.dto.messaging.UnreadCountResponse;
import com.atamanahmet.vinylexchange.dto.musicbrainz.ArtistCredit;
import com.atamanahmet.vinylexchange.dto.musicbrainz.Tags;
import com.atamanahmet.vinylexchange.dto.notification.NotificationDTO;
import com.atamanahmet.vinylexchange.dto.notification.NotificationResponse;
import com.atamanahmet.vinylexchange.dto.order.CartValidationIssue;
import com.atamanahmet.vinylexchange.dto.order.CartValidationResult;
import com.atamanahmet.vinylexchange.dto.order.CheckOutresultDTO;
import com.atamanahmet.vinylexchange.dto.user.TradePreferenceDTO;
import com.atamanahmet.vinylexchange.dto.user.TradePreferenceRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecordDtoSerializationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void pageDto_roundTrip() throws Exception {
        PageDTO original = new PageDTO("About", "We sell vinyl", "/images/about.jpg");
        assertThat(mapper.readValue(mapper.writeValueAsString(original), PageDTO.class)).isEqualTo(original);
    }

    @Test
    void addToWishlistRequest_roundTrip() throws Exception {
        AddToWishlistRequest original = new AddToWishlistRequest("Abbey Road", "The Beatles", 1969, "UK", "Apple", null, null, "LP");
        assertThat(mapper.readValue(mapper.writeValueAsString(original), AddToWishlistRequest.class)).isEqualTo(original);
    }

    @Test
    void addToWishlistBulkRequest_roundTrip() throws Exception {
        AddToWishlistRequest item = new AddToWishlistRequest("OK Computer", "Radiohead", 1997, null, null, null, null, "LP");
        AddToWishlistBulkRequest original = new AddToWishlistBulkRequest(List.of(item));
        AddToWishlistBulkRequest restored = mapper.readValue(mapper.writeValueAsString(original), AddToWishlistBulkRequest.class);
        assertThat(restored.bulkRequest()).hasSize(1);
        assertThat(restored.bulkRequest().get(0).title()).isEqualTo("OK Computer");
    }

    @Test
    void addToWishlistFailureDto_roundTrip() throws Exception {
        AddToWishlistRequest request = new AddToWishlistRequest("Title", "Artist", null, null, null, null, null, null);
        AddToWishlistFailureDTO original = new AddToWishlistFailureDTO(request, "duplicate");
        AddToWishlistFailureDTO restored = mapper.readValue(mapper.writeValueAsString(original), AddToWishlistFailureDTO.class);
        assertThat(restored.reason()).isEqualTo("duplicate");
        assertThat(restored.request().artist()).isEqualTo("Artist");
    }

    @Test
    void unreadCountResponse_roundTrip() throws Exception {
        UnreadCountResponse original = new UnreadCountResponse(7L);
        assertThat(mapper.readValue(mapper.writeValueAsString(original), UnreadCountResponse.class).unreadCount()).isEqualTo(7L);
    }

    @Test
    void artistCredit_roundTrip() throws Exception {
        ArtistCredit original = new ArtistCredit("The Beatles");
        assertThat(mapper.readValue(mapper.writeValueAsString(original), ArtistCredit.class)).isEqualTo(original);
    }

    @Test
    void tags_roundTrip() throws Exception {
        Tags original = new Tags("rock");
        assertThat(mapper.readValue(mapper.writeValueAsString(original), Tags.class).name()).isEqualTo("rock");
    }

    @Test
    void notificationDto_builderAndJson() throws Exception {
        UUID id = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        NotificationDTO original = NotificationDTO.builder()
                .id(id)
                .title("Match")
                .message("Your wishlist item is listed")
                .read(false)
                .createdAt(LocalDateTime.of(2025, 1, 15, 10, 0))
                .relatedListingId(listingId)
                .build();

        NotificationDTO restored = mapper.readValue(mapper.writeValueAsString(original), NotificationDTO.class);

        assertThat(restored.id()).isEqualTo(id);
        assertThat(restored.read()).isFalse();
        assertThat(restored.relatedListingId()).isEqualTo(listingId);
    }

    @Test
    void notificationResponse_roundTrip() throws Exception {
        NotificationDTO dto = NotificationDTO.builder()
                .id(UUID.randomUUID())
                .title("t")
                .message("m")
                .read(true)
                .build();
        NotificationResponse original = new NotificationResponse(List.of(dto), 3);
        NotificationResponse restored = mapper.readValue(mapper.writeValueAsString(original), NotificationResponse.class);
        assertThat(restored.unreadCount()).isEqualTo(3);
        assertThat(restored.notifications()).hasSize(1);
    }

    @Test
    void cartValidationIssue_builderRoundTrip() throws Exception {
        UUID cartItemId = UUID.randomUUID();
        CartValidationIssue original = CartValidationIssue.builder()
                .cartItemId(cartItemId)
                .listingId(UUID.randomUUID())
                .type(IssueType.LISTING_DELETED)
                .message("sold out")
                .build();

        CartValidationIssue restored = mapper.readValue(mapper.writeValueAsString(original), CartValidationIssue.class);
        assertThat(restored.cartItemId()).isEqualTo(cartItemId);
        assertThat(restored.type()).isEqualTo(IssueType.LISTING_DELETED);
    }

    @Test
    void cartValidationResult_builderCreatesRecord() {
        CartValidationIssue issue = CartValidationIssue.builder()
                .cartItemId(UUID.randomUUID())
                .listingId(UUID.randomUUID())
                .type(IssueType.LISTING_DELETED)
                .message("unavailable")
                .build();

        CartValidationResult result = CartValidationResult.builder()
                .issues(List.of(issue))
                .hasErrors(true)
                .build();

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.issues()).hasSize(1);
        assertThat(result.issues().get(0).message()).isEqualTo("unavailable");
    }

    @Test
    void checkOutResultDto_roundTrip() throws Exception {
        UUID orderId = UUID.randomUUID();
        CheckOutresultDTO original = new CheckOutresultDTO(true, "ok", orderId);
        CheckOutresultDTO restored = mapper.readValue(mapper.writeValueAsString(original), CheckOutresultDTO.class);
        assertThat(restored.success()).isTrue();
        assertThat(restored.orderId()).isEqualTo(orderId);
    }

    @Test
    void tradePreferenceRequest_roundTrip() throws Exception {
        TradePreferenceRequest original = new TradePreferenceRequest("Dark Side of the Moon", 50.0, PaymentDirection.PAY);
        TradePreferenceRequest restored = mapper.readValue(mapper.writeValueAsString(original), TradePreferenceRequest.class);
        assertThat(restored.desiredItem()).isEqualTo("Dark Side of the Moon");
        assertThat(restored.paymentDirection()).isEqualTo(PaymentDirection.PAY);
    }

    @Test
    void tradePreferenceRequest_compactCtorDefaults() {
        TradePreferenceRequest req = new TradePreferenceRequest("Item", null, null);
        assertThat(req.extraAmount()).isEqualTo(0.0);
        assertThat(req.paymentDirection()).isEqualTo(PaymentDirection.NO_EXTRA);
    }

    @Test
    void addToWishlistRequest_partialJson_missingOptionalsStayNull() throws Exception {
        String json = "{\"title\":\"Abbey Road\",\"artist\":\"The Beatles\"}";
        AddToWishlistRequest req = mapper.readValue(json, AddToWishlistRequest.class);
        AddToWishlistRequest expected = new AddToWishlistRequest(
                "Abbey Road", "The Beatles", null, null, null, null, null, null);
        assertThat(req).isEqualTo(expected);
    }

    @Test
    void tradePreferenceRequest_partialJson_appliesCompactCtorDefaults() throws Exception {
        String json = "{\"desiredItem\":\"Wish Album\"}";
        TradePreferenceRequest req = mapper.readValue(json, TradePreferenceRequest.class);
        assertThat(req.desiredItem()).isEqualTo("Wish Album");
        assertThat(req.extraAmount()).isEqualTo(0.0);
        assertThat(req.paymentDirection()).isEqualTo(PaymentDirection.NO_EXTRA);
    }

    @Test
    void tradePreferenceDto_fromEntity() {
        TradePreference entity = new TradePreference();
        entity.setId(UUID.randomUUID());
        entity.setDesiredItem("Wish Album");
        entity.setExtraAmount(25.0);
        entity.setPaymentDirection(PaymentDirection.RECEIVE);

        TradePreferenceDTO dto = new TradePreferenceDTO(entity);

        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.desiredItem()).isEqualTo("Wish Album");
        assertThat(dto.extraAmount()).isEqualTo(25.0);
        assertThat(dto.paymentDirection()).isEqualTo(PaymentDirection.RECEIVE);
    }

    @Test
    void tradePreferenceDto_fromEntitiesReturnsEmptyForNull() {
        assertThat(TradePreferenceDTO.fromEntities(null)).isEmpty();
        assertThat(TradePreferenceDTO.fromEntities(List.of())).isEmpty();
    }
}

package com.atamanahmet.vinylexchange.config;

import com.atamanahmet.vinylexchange.common.NanoIdGenerator;
import com.atamanahmet.vinylexchange.common.money.ListingPriceCalculator;
import com.atamanahmet.vinylexchange.common.money.ListingPriceResult;
import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.ListingImage;
import com.atamanahmet.vinylexchange.domain.entity.Page;
import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.domain.enums.ListingStatus;
import com.atamanahmet.vinylexchange.domain.enums.PageType;
import com.atamanahmet.vinylexchange.domain.enums.RoleName;
import com.atamanahmet.vinylexchange.domain.enums.SaleType;
import com.atamanahmet.vinylexchange.domain.enums.StorageProvider;
import com.atamanahmet.vinylexchange.dto.user.RegisterRequest;
import com.atamanahmet.vinylexchange.infrastructure.search.service.BulkListingIndexService;
import com.atamanahmet.vinylexchange.repository.GenreRepository;
import com.atamanahmet.vinylexchange.repository.listing.ListingRepository;
import com.atamanahmet.vinylexchange.service.CmsService;
import com.atamanahmet.vinylexchange.service.media.DemoCoverService;
import com.atamanahmet.vinylexchange.service.media.FileStorageService;
import com.atamanahmet.vinylexchange.service.user.AuthService;
import com.atamanahmet.vinylexchange.service.user.RoleService;
import com.atamanahmet.vinylexchange.service.user.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Seeds demo data on every startup in local dev.
 * Creates admin user, mock listings with fixed ids, local cover images, and CMS pages.
 * Triggers bulk OpenSearch index after seeding.
 * Runs after RoleInitializer (Order 1) and GenreInitializer (Order 2).
 */
@Slf4j
@Component
@Order(3)
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

        @Value("${file.upload-cms-dir}")
        private String uploadCmsDir;

        @Value("${admin.test.password}")
        private String adminTestPassword;

        @Value("${admin.test.email}")
        private String adminTestEmail;

        @Value("${app.base-url}")
        private String baseUrl;

        private final UserService userService;
        private final AuthService authService;
        private final ListingRepository listingRepository;
        private final RoleService roleService;
        private final CmsService cmsService;
        private final EntityManager entityManager;
        private final BulkListingIndexService bulkListingIndexService;
        private final DemoCoverService demoCoverService;
        private final FileStorageService fileStorageService;
        private final GenreRepository genreRepository;
        private final ListingPriceCalculator priceCalculator;
        private final CacheManager cacheManager;

        @Override
        @Transactional
        public void run(ApplicationArguments args) {
                createDatabaseSequences();
                migrateListingMediaSchema();
                User adminUser = ensureAdminUser();
                demoCoverService.syncAllDemoCovers();
                seedMockListings(adminUser);
                seedCmsPages();
                bulkListingIndexService.indexAllListings();
        }

        private void createDatabaseSequences() {
                try {
                        entityManager.createNativeQuery(
                                        "CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 10000 INCREMENT BY 1")
                                .executeUpdate();
                        log.info("order_sequence_ready");
                } catch (Exception e) {
                        log.error("order_sequence_creation_failed reason={}", e.getMessage(), e);
                }
        }

        /**
         * Dev-only: ddl-auto update does not always add embeddable columns when legacy exists.
         */
        private void migrateListingMediaSchema() {
                List<String> statements = List.of(
                                "ALTER TABLE listings ADD COLUMN IF NOT EXISTS media_info_format VARCHAR(255)",
                                "ALTER TABLE listings ADD COLUMN IF NOT EXISTS media_info_vinyl_subtype VARCHAR(255)",
                                "ALTER TABLE listings ADD COLUMN IF NOT EXISTS media_info_speed_rpm INTEGER",
                                "ALTER TABLE listings ADD COLUMN IF NOT EXISTS media_info_vinyl_size VARCHAR(32)",
                                "ALTER TABLE listings ADD COLUMN IF NOT EXISTS media_info_disc_count INTEGER",
                                "ALTER TABLE listings ADD COLUMN IF NOT EXISTS media_info_colored BOOLEAN",
                                "ALTER TABLE listings ADD COLUMN IF NOT EXISTS media_info_picture_disc BOOLEAN",
                                "ALTER TABLE listings ADD COLUMN IF NOT EXISTS media_info_source_format_raw VARCHAR(255)",
                                "ALTER TABLE listings DROP COLUMN IF EXISTS format");

                for (String sql : statements) {
                        try {
                                entityManager.createNativeQuery(sql).executeUpdate();
                        } catch (Exception e) {
                                log.warn("listing_media_schema_step_skipped sql={} reason={}", sql, e.getMessage());
                        }
                }

                entityManager.flush();
                log.info("listing_media_schema_ready");
        }

        private User ensureAdminUser() {
                Optional<User> adminUser = userService.findAdmin();

                if (adminUser.isEmpty()) {
                        log.info("admin_not_found creating_admin");
                        authService.registerUser(
                                new RegisterRequest("admin", adminTestPassword, adminTestEmail),
                                roleService.getRoleByName(RoleName.ROLE_ADMIN));
                        adminUser = userService.findAdmin();
                }

                log.info("admin_ready");
                return adminUser.get();
        }


        private void seedMockListings(User adminUser) {
                log.info("seeding_mock_listings");

                int createdCount = 0;
                for (DemoListingCatalog.DemoListing demo : DemoListingCatalog.ALL) {
                        if (!listingRepository.existsById(demo.id())) {
                                insertDemoListingRow(demo, adminUser);
                                createdCount++;
                                log.info("demo_listing_created id={} title={}", demo.id(), demo.title());
                        }

                        listingRepository.findByIdWithImages(demo.id()).ifPresent(listing -> {
                                if (listing.getImages().isEmpty()) {
                                        wireImages(listing, demo.id());
                                        listingRepository.save(listing);
                                        log.info("demo_listing_images_wired id={} title={}", demo.id(), demo.title());
                                }
                                wireGenres(listing, demo);
                        });
                }

                if (createdCount > 0) {
                        // create-drop / empty DB: new NanoId publicIds invalidate any Redis listing entries
                        evictListingsCache(createdCount);
                }

                log.info("mock_listings_seeded created={}", createdCount);
        }

        private void evictListingsCache(int createdCount) {
                Cache cache = cacheManager.getCache("listings");
                if (cache == null) {
                        log.warn("listings_cache_missing skip_evict after_demo_seed created={}", createdCount);
                        return;
                }
                cache.clear();
                log.info("listings_cache_cleared reason=demo_listings_created count={}", createdCount);
        }

        /**
         * Native insert for fixed demo ids, JPA save/persist fails with pre-assigned UUIDs on @GeneratedValue.
         */
        private void insertDemoListingRow(DemoListingCatalog.DemoListing demo, User owner) {
                ListingPriceResult price = priceCalculator.fromBuyerPrice(demo.priceKurus());
                LocalDateTime now = LocalDateTime.now();
                String publicId = NanoIdGenerator.generate();

                entityManager.createNativeQuery("""
                                INSERT INTO listings (
                                    id, public_id, title, artist_name, label_name, country, year, condition,
                                    media_info_format, media_info_vinyl_subtype, media_info_speed_rpm,
                                    media_info_vinyl_size, media_info_disc_count,
                                    price_kurus, seller_earnings_kurus, platform_cut_kurus, platform_fee_bp,
                                    original_price_kurus, owner_id, status, stock_quantity, on_hold,
                                    sale_type, promote, needs_image_migration, trade_value, created_at, updated_at
                                ) VALUES (
                                    :id, :publicId, :title, :artistName, :labelName, :country, :year, :condition,
                                    :mediaFormat, :vinylSubtype, :speedRpm, :vinylSize, :discCount,
                                    :priceKurus, :sellerEarningsKurus, :platformCutKurus, :platformFeeBp,
                                    :originalPriceKurus, :ownerId, :status, :stockQuantity, :onHold,
                                    :saleType, :promote, false, :tradeValue, :createdAt, :updatedAt
                                )
                                """)
                        .setParameter("id", demo.id())
                        .setParameter("title", demo.title())
                        .setParameter("artistName", demo.artistName())
                        .setParameter("labelName", demo.labelName())
                        .setParameter("country", demo.country().name())
                        .setParameter("year", demo.year())
                        .setParameter("condition", demo.condition())
                        .setParameter("mediaFormat", demo.mediaInfo().getFormat().name())
                        .setParameter("vinylSubtype",
                                demo.mediaInfo().getVinylSubtype() != null
                                        ? demo.mediaInfo().getVinylSubtype().name()
                                        : null)
                        .setParameter("speedRpm", demo.mediaInfo().getSpeedRpm())
                        .setParameter("vinylSize", demo.mediaInfo().getVinylSize())
                        .setParameter("discCount", demo.mediaInfo().getDiscCount())
                        .setParameter("priceKurus", price.priceKurus())
                        .setParameter("sellerEarningsKurus", price.sellerEarningsKurus())
                        .setParameter("platformCutKurus", price.platformCutKurus())
                        .setParameter("platformFeeBp", price.feeBP())
                        .setParameter("originalPriceKurus", price.priceKurus())
                        .setParameter("ownerId", owner.getId())
                        .setParameter("status", ListingStatus.AVAILABLE.name())
                        .setParameter("stockQuantity", 5)
                        .setParameter("onHold", false)
                        .setParameter("saleType", SaleType.FIXED_PRICE.name())
                        .setParameter("promote", false)
                        .setParameter("tradeValue", 0L)
                        .setParameter("createdAt", now)
                        .setParameter("updatedAt", now)
                        .setParameter("publicId", publicId)
                        .executeUpdate();

                entityManager.flush();
        }

        private void wireImages(Listing listing, UUID listingId) {
                List<String> imageUrls = fileStorageService.getListingImagePaths(listingId);
                if (imageUrls.isEmpty()) {
                        log.warn("demo_listing_no_images id={} title={}", listingId, listing.getTitle());
                        return;
                }

                int position = 0;
                for (String url : imageUrls) {
                        listing.getImages().add(ListingImage.builder()
                                .publicId(url)
                                .secureUrl(url)
                                .position(position++)
                                .provider(StorageProvider.LOCAL)
                                .uploadedAt(LocalDateTime.now())
                                .listing(listing)
                                .build());
                }
                listing.setMainImageUrl(imageUrls.get(0));
        }

        private void wireGenres(Listing listing, DemoListingCatalog.DemoListing demo) {
                if (!listing.getGenres().isEmpty()) {
                        return;
                }

                for (String genreName : demo.genreNames()) {
                        genreRepository.findByName(genreName)
                                        .ifPresent(genre -> listing.getGenres().add(genre));
                }

                listingRepository.save(listing);
                log.info("demo_listing_genres_wired id={} title={}", demo.id(), demo.title());
        }

        private void seedCmsPages() {
                if (cmsService.existsByPageType(PageType.ABOUT)) {
                        log.info("cms_about_page_exists skipping");
                        return;
                }

                log.info("seeding_cms_about_page");

                String aboutPageImagePath = baseUrl + "/" + uploadCmsDir + "about/about.jpg";
                String textContentPath = baseUrl + "/" + uploadCmsDir + "about/content.txt";

                cmsService.savePage(Page.builder()
                        .pageType(PageType.ABOUT)
                        .header("About Vinyl Exchange")
                        .textContentPath(textContentPath)
                        .backgroundColor("bg-indigo-700")
                        .backgroundImagePath(aboutPageImagePath)
                        .build());

                log.info("cms_about_page_seeded");
        }
}

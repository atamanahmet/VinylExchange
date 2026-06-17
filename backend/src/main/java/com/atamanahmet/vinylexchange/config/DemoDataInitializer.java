package com.atamanahmet.vinylexchange.config;

import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.domain.entity.Page;
import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.domain.enums.PageType;
import com.atamanahmet.vinylexchange.domain.enums.RoleName;
import com.atamanahmet.vinylexchange.dto.user.RegisterRequest;
import com.atamanahmet.vinylexchange.infrastructure.search.service.BulkListingIndexService;
import com.atamanahmet.vinylexchange.service.CmsService;
import com.atamanahmet.vinylexchange.service.listing.ListingService;
import com.atamanahmet.vinylexchange.service.user.AuthService;
import com.atamanahmet.vinylexchange.service.user.RoleService;
import com.atamanahmet.vinylexchange.service.user.UserService;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Seeds demo data on every startup.
 * Creates admin user, mock listings, and CMS pages if not already present.
 * Triggers bulk OpenSearch index after seeding.
 * Runs after RoleInitializer (Order 2).
 */
@Slf4j
@Component
@Order(2)
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
        private final ListingService listingService;
        private final RoleService roleService;
        private final CmsService cmsService;
        private final EntityManager entityManager;
        private final BulkListingIndexService bulkListingIndexService;

        @Override
        @Transactional
        public void run(ApplicationArguments args) {
                createDatabaseSequences();
                User adminUser = ensureAdminUser();
                seedMockListings(adminUser);
                seedCmsPages();
                bulkListingIndexService.indexAllListings();
        }

        /**
         * Creates order number sequence if not exists.
         * Native query used because JPA does not support sequence DDL.
         */
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
         * Creates admin user if not present.
         * Admin is the owner of all mock listings.
         */
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

        /**
         * Seeds mock vinyl listings owned by admin.
         * Skips listings that already exist by title.
         */
        private void seedMockListings(User adminUser) {
                log.info("seeding_mock_listings");

                List<Listing> mockListings = generateMockListings(adminUser);

                mockListings.forEach(listing -> {
                        if (!listingService.isExistByTitle(listing.getTitle())) {
                                listingService.saveListing(listing);
                        }
                });

                log.info("mock_listings_seeded");
        }

        /**
         * Seeds CMS about page if not already present.
         */
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

        private List<Listing> generateMockListings(User adminUser) {
                return List.of(
                        Listing.builder().title("Abbey Road").artistName("The Beatles")
                                .labelName("Apple Records").format("LP").country("UK")
                                .year(1969).condition("VG").priceKurus(90000).owner(adminUser).build(),
                        Listing.builder().title("Nevermind").artistName("Nirvana")
                                .labelName("DGC").format("LP").country("US")
                                .year(1991).condition("NM").priceKurus(78000).owner(adminUser).build(),
                        Listing.builder().title("OK Computer").artistName("Radiohead")
                                .labelName("Parlophone").format("LP").country("UK")
                                .year(1997).condition("NM").priceKurus(82000).owner(adminUser).build(),
                        Listing.builder().title("In Rainbows").artistName("Radiohead")
                                .labelName("XL Recordings").format("LP").country("EU")
                                .year(2007).condition("NM").priceKurus(76000).owner(adminUser).build(),
                        Listing.builder().title("The Wall").artistName("Pink Floyd")
                                .labelName("Harvest").format("2xLP").country("UK")
                                .year(1979).condition("VG+").priceKurus(95000).owner(adminUser).build(),
                        Listing.builder().title("Kind of Blue").artistName("Miles Davis")
                                .labelName("Columbia").format("LP").country("US")
                                .year(1959).condition("VG+").priceKurus(88000).owner(adminUser).build(),
                        Listing.builder().title("Blue Train").artistName("John Coltrane")
                                .labelName("Blue Note").format("LP").country("US")
                                .year(1957).condition("VG").priceKurus(92000).owner(adminUser).build(),
                        Listing.builder().title("Back to Black").artistName("Amy Winehouse")
                                .labelName("Island Records").format("LP").country("EU")
                                .year(2006).condition("NM").priceKurus(68000).owner(adminUser).build(),
                        Listing.builder().title("Rumours").artistName("Fleetwood Mac")
                                .labelName("Warner Bros.").format("LP").country("US")
                                .year(1977).condition("VG+").priceKurus(74000).owner(adminUser).build(),
                        Listing.builder().title("Led Zeppelin IV").artistName("Led Zeppelin")
                                .labelName("Atlantic").format("LP").country("UK")
                                .year(1971).condition("VG").priceKurus(86000).owner(adminUser).build(),
                        Listing.builder().title("Master of Puppets").artistName("Metallica")
                                .labelName("Elektra").format("LP").country("US")
                                .year(1986).condition("NM").priceKurus(83000).owner(adminUser).build(),
                        Listing.builder().title("Revolver").artistName("The Beatles")
                                .labelName("Parlophone").format("LP").country("UK")
                                .year(1966).condition("VG").priceKurus(89000).owner(adminUser).build(),
                        Listing.builder().title("Unknown Pleasures").artistName("Joy Division")
                                .labelName("Factory").format("LP").country("UK")
                                .year(1979).condition("NM").priceKurus(81000).owner(adminUser).build(),
                        Listing.builder().title("Disintegration").artistName("The Cure")
                                .labelName("Fiction").format("LP").country("UK")
                                .year(1989).condition("NM").priceKurus(79000).owner(adminUser).build(),
                        Listing.builder().title("The Rise and Fall of Ziggy Stardust").artistName("David Bowie")
                                .labelName("RCA").format("LP").country("UK")
                                .year(1972).condition("VG+").priceKurus(84000).owner(adminUser).build(),
                        Listing.builder().title("Discovery").artistName("Daft Punk")
                                .labelName("Virgin").format("LP").country("EU")
                                .year(2001).condition("NM").priceKurus(72000).owner(adminUser).build(),
                        Listing.builder().title("To Pimp a Butterfly").artistName("Kendrick Lamar")
                                .labelName("Top Dawg").format("LP").country("US")
                                .year(2015).condition("NM").priceKurus(87000).owner(adminUser).build(),
                        Listing.builder().title("Mezzanine").artistName("Massive Attack")
                                .labelName("Virgin").format("LP").country("UK")
                                .year(1998).condition("NM").priceKurus(80000).owner(adminUser).build(),
                        Listing.builder().title("Violator").artistName("Depeche Mode")
                                .labelName("Mute").format("LP").country("UK")
                                .year(1990).condition("VG+").priceKurus(76000).owner(adminUser).build());
        }
}
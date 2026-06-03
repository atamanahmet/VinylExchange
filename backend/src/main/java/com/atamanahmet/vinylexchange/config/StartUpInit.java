package com.atamanahmet.vinylexchange.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.atamanahmet.vinylexchange.service.user.AuthService;
import com.atamanahmet.vinylexchange.domain.entity.Role;
import com.atamanahmet.vinylexchange.service.user.RoleService;
import com.atamanahmet.vinylexchange.dto.user.RegisterRequest;
import com.atamanahmet.vinylexchange.domain.enums.RoleName;
import com.atamanahmet.vinylexchange.service.CmsService;
import com.atamanahmet.vinylexchange.domain.entity.Page;
import com.atamanahmet.vinylexchange.domain.enums.PageType;
import com.atamanahmet.vinylexchange.service.listing.BulkListingIndexService;
import com.atamanahmet.vinylexchange.domain.entity.Listing;
import com.atamanahmet.vinylexchange.service.listing.ListingService;
import com.atamanahmet.vinylexchange.domain.NotificationCommand;
import com.atamanahmet.vinylexchange.service.NotificationService;
import com.atamanahmet.vinylexchange.domain.enums.NotificationType;

import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.service.user.UserService;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@Order(2)
@RequiredArgsConstructor
// TODO remove, development only
public class StartUpInit implements ApplicationRunner {

        @Value("${file.upload-cms-dir}")
        private String UPLOAD_CMS_DIR;

        @Value("${admin.test.password}")
        private String ADMIN_TEST_PASSWORD;

        @Value("${admin.test.email}")
        private String ADMIN_TEST_EMAIL;

        @Value("${app.base-url}")
        private String BASE_URL;

        private boolean isBulkIndexEnabled = true;

        private Logger logger = LoggerFactory.getLogger(StartUpInit.class);

        private final UserService userService;
        private final AuthService authService;
        private final ListingService listingService;
        private final RoleService roleService;
        private final CmsService cmsService;
        private final EntityManager entityManager;
        private final NotificationService notificationService;
        private final BulkListingIndexService bulkListingIndexService;

        private final RestHighLevelClient openSearchClient;


        @Override
        @Transactional
        public void run(ApplicationArguments args) {

                // orderRepository.deleteAll();
                // orderItemRepository.deleteAll();

                createDatabaseSequences();

                logger.info("Initializing mock up listings...");

                Optional<User> adminUser = userService.findAdmin();

                if (!adminUser.isPresent()) {

                        logger.info("admin user not present, creating admin...");

                        Role adminRole = roleService.getRoleByName(RoleName.ROLE_ADMIN);

                        authService.registerUser(
                                        new RegisterRequest("admin", ADMIN_TEST_PASSWORD, ADMIN_TEST_EMAIL),
                                        adminRole);

                        adminUser = userService.findAdmin();

                }

                logger.info("admin present, initiating mock listings...");

                List<Listing> mockList = generateMockListings(adminUser.get());

                for (Listing listing : mockList) {
                        if (!listingService.isExistByTitle(listing.getTitle())) {
                                listingService.saveListing(listing);
                        }
                }

                logger.info("mock listings created/checked...");

                createCMSPages();
                createMockUpNotification(adminUser.get().getId());

                createSearchIndex();

            if (isBulkIndexEnabled) {
                        bulkListingIndexService.indexAllListings();
                }
        }

        private void createSearchIndex() {
                try {
                        String INDEX_NAME = "listings";

                        boolean exists = openSearchClient.indices()
                                        .exists(new GetIndexRequest(INDEX_NAME), RequestOptions.DEFAULT);

                        if (!exists) {
                                logger.info("Creating search index '{}'...", INDEX_NAME);

                                CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX_NAME);

                                createIndexRequest.settings(Settings.builder()
                                                .put("analysis.analyzer.autocomplete.tokenizer",
                                                                "autocomplete_tokenizer")
                                                .putList("analysis.analyzer.autocomplete.filter", "lowercase")
                                                .put("analysis.tokenizer.autocomplete_tokenizer.type", "edge_ngram")
                                                .put("analysis.tokenizer.autocomplete_tokenizer.min_gram", 3)
                                                .put("analysis.tokenizer.autocomplete_tokenizer.max_gram", 20)
                                                .putList("analysis.tokenizer.autocomplete_tokenizer.token_chars",
                                                                "letter", "digit"));

                                String mappingJson = "{\n" +
                                                "  \"properties\": {\n" +
                                                "    \"title\": {\"type\":\"text\", \"analyzer\":\"autocomplete\", \"search_analyzer\":\"standard\"},\n"
                                                +
                                                "    \"artistName\": {\"type\":\"text\", \"analyzer\":\"autocomplete\", \"search_analyzer\":\"standard\"},\n"
                                                +
                                                "    \"labelName\": {\"type\":\"text\"},\n" +
                                                "    \"status\": {\"type\":\"keyword\"},\n" +
                                                "    \"createdAt\": {\"type\":\"date\"},\n" +
                                                "    \"price\": {\"type\":\"long\"}\n" +
                                                "  }\n" +
                                                "}";

                                createIndexRequest.mapping(mappingJson, XContentType.JSON);

                                CreateIndexResponse response = openSearchClient.indices()
                                                .create(createIndexRequest, RequestOptions.DEFAULT);

                                logger.info("Search index '{}' created successfully: {}", INDEX_NAME,
                                                response.isAcknowledged());
                        } else {
                                logger.info("Search index '{}' already exists", INDEX_NAME);
                        }
                } catch (Exception e) {
                        logger.error("Failed to create search index: {}", e.getMessage(), e);
                }
        }

        private void createMockUpNotification(UUID userId) {
                notificationService.clearAll();
                UUID listingId = listingService.getAllListings().get(0).getId();

                NotificationCommand command = new NotificationCommand(NotificationType.INFO, "Info title",
                                "Your wishlist item just added to collection", listingId);

                notificationService.createNotification(userId, command);

                NotificationCommand command2 = new NotificationCommand(NotificationType.INFO, "Info title",
                                "Record almost sold out", listingId);

                notificationService.createNotification(userId, command2);

                logger.info("mock notification created");
        }

        private void createDatabaseSequences() {
                try {
                        entityManager.createNativeQuery(
                                        "CREATE SEQUENCE IF NOT EXISTS order_number_seq " +
                                                        "START WITH 10000 INCREMENT BY 1")
                                        .executeUpdate();

                        System.out.println("  orderseq ready");

                } catch (Exception e) {
                        System.out.println("  orderseq creation error: " + e.getMessage());
                }
        }

        public void createCMSPages() {
                logger.info("Initializing about page...");

                cmsService.deleteAll();

                // if (!cmsService.existsByPageType(PageType.ABOUT)) {
                // logger.info("about page not found, creating");

                String aboutPageImagePath = BASE_URL + "/" + UPLOAD_CMS_DIR + "about/about.jpg";
                String textContentPath = BASE_URL + "/" + UPLOAD_CMS_DIR + "about/content.txt";

                Page aboutPage = Page.builder()
                                .pageType(PageType.ABOUT)
                                .header("About Vinyl Exchange")
                                .textContentPath(textContentPath)
                                .backgroundColor("bg-indigo-700")
                                .backgroundImagePath(aboutPageImagePath)
                                .build();

                cmsService.savePage(aboutPage);
                // }
        }

        public List<Listing> generateMockListings(User adminUser) {
                List<Listing> list = new ArrayList<>();

                list.addAll(List.of(
                                Listing.builder()
                                                .title("Abbey Road")
                                                .artistName("The Beatles")
                                                .labelName("Apple Records")
                                                .format("LP")
                                                .country("UK")
                                                .year(1969)
                                                .condition("VG")
                                                .priceKurus(90000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Nevermind")
                                                .artistName("Nirvana")
                                                .labelName("DGC")
                                                .format("LP")
                                                .country("US")
                                                .year(1991)
                                                .condition("NM")
                                                .priceKurus(78000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("OK Computer")
                                                .artistName("Radiohead")
                                                .labelName("Parlophone")
                                                .format("LP")
                                                .country("UK")
                                                .year(1997)
                                                .condition("NM")
                                                .priceKurus(82000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("In Rainbows")
                                                .artistName("Radiohead")
                                                .labelName("XL Recordings")
                                                .format("LP")
                                                .country("EU")
                                                .year(2007)
                                                .condition("NM")
                                                .priceKurus(76000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("The Wall")
                                                .artistName("Pink Floyd")
                                                .labelName("Harvest")
                                                .format("2xLP")
                                                .country("UK")
                                                .year(1979)
                                                .condition("VG+")
                                                .priceKurus(95000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Kind of Blue")
                                                .artistName("Miles Davis")
                                                .labelName("Columbia")
                                                .format("LP")
                                                .country("US")
                                                .year(1959)
                                                .condition("VG+")
                                                .priceKurus(88000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Blue Train")
                                                .artistName("John Coltrane")
                                                .labelName("Blue Note")
                                                .format("LP")
                                                .country("US")
                                                .year(1957)
                                                .condition("VG")
                                                .priceKurus(92000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Back to Black")
                                                .artistName("Amy Winehouse")
                                                .labelName("Island Records")
                                                .format("LP")
                                                .country("EU")
                                                .year(2006)
                                                .condition("NM")
                                                .priceKurus(68000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Rumours")
                                                .artistName("Fleetwood Mac")
                                                .labelName("Warner Bros.")
                                                .format("LP")
                                                .country("US")
                                                .year(1977)
                                                .condition("VG+")
                                                .priceKurus(74000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Led Zeppelin IV")
                                                .artistName("Led Zeppelin")
                                                .labelName("Atlantic")
                                                .format("LP")
                                                .country("UK")
                                                .year(1971)
                                                .condition("VG")
                                                .priceKurus(86000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Master of Puppets")
                                                .artistName("Metallica")
                                                .labelName("Elektra")
                                                .format("LP")
                                                .country("US")
                                                .year(1986)
                                                .condition("NM")
                                                .priceKurus(83000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Revolver")
                                                .artistName("The Beatles")
                                                .labelName("Parlophone")
                                                .format("LP")
                                                .country("UK")
                                                .year(1966)
                                                .condition("VG")
                                                .priceKurus(89000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Unknown Pleasures")
                                                .artistName("Joy Division")
                                                .labelName("Factory")
                                                .format("LP")
                                                .country("UK")
                                                .year(1979)
                                                .condition("NM")
                                                .priceKurus(81000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Disintegration")
                                                .artistName("The Cure")
                                                .labelName("Fiction")
                                                .format("LP")
                                                .country("UK")
                                                .year(1989)
                                                .condition("NM")
                                                .priceKurus(79000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("The Rise and Fall of Ziggy Stardust")
                                                .artistName("David Bowie")
                                                .labelName("RCA")
                                                .format("LP")
                                                .country("UK")
                                                .year(1972)
                                                .condition("VG+")
                                                .priceKurus(84000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Discovery")
                                                .artistName("Daft Punk")
                                                .labelName("Virgin")
                                                .format("LP")
                                                .country("EU")
                                                .year(2001)
                                                .condition("NM")
                                                .priceKurus(72000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("To Pimp a Butterfly")
                                                .artistName("Kendrick Lamar")
                                                .labelName("Top Dawg")
                                                .format("LP")
                                                .country("US")
                                                .year(2015)
                                                .condition("NM")
                                                .priceKurus(87000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Mezzanine")
                                                .artistName("Massive Attack")
                                                .labelName("Virgin")
                                                .format("LP")
                                                .country("UK")
                                                .year(1998)
                                                .condition("NM")
                                                .priceKurus(80000)
                                                .owner(adminUser)
                                                .build(),

                                Listing.builder()
                                                .title("Violator")
                                                .artistName("Depeche Mode")
                                                .labelName("Mute")
                                                .format("LP")
                                                .country("UK")
                                                .year(1990)
                                                .condition("VG+")
                                                .priceKurus(76000)
                                                .owner(adminUser)
                                                .build()));

                return list;

        }

}

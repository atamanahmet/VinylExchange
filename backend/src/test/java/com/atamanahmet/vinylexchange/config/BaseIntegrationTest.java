package com.atamanahmet.vinylexchange.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "test"})
@Testcontainers
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("vinylexchange_test")
            .withUsername("test")
            .withPassword("test");

    static final VaultContainer<?> vault =
        new VaultContainer<>("hashicorp/vault:1.15")
            .withVaultToken("test-root-token")
            .waitingFor(Wait.forHttp("/v1/sys/health").forPort(8200));

    static {
        postgres.start();
        vault.start();
        seedVault();
    }

    /**
     * Seeds Vault container with all secrets required by the application.
     * Uses the root token and HTTP API via vault CLI inside the container.
     */
    private static void seedVault() {
        try {
            vault.execInContainer(
                "vault", "kv", "put", "secret/vinyl-exchange",
                "aes.encryption.key=dGVzdGtleXRlc3RrZXl0ZXN0a2V5dGVzdGtleXRlc3Q=",
                "admin.test.email=admin@test.com",
                "admin.test.password=testpassword123"
            );

            vault.execInContainer(
                "vault", "kv", "put", "secret/vinyl-exchange/dev",
                "spring.datasource.username=test",
                "spring.datasource.password=test",
                "jwt.secret=dGVzdGp3dHNlY3JldGtleXRlc3Rqd3RzZWNyZXRrZXl0ZXN0and0c2VjcmV0a2V5dGVzdA==",
                "payment.iyzico.api-key=test",
                "payment.iyzico.secret-key=test",
                "payment.iyzico.base-url=http://localhost",
                "payment.iyzico.callback-url=http://localhost/callback",
                "cloudinary.url=cloudinary://test_key:test_secret@test_cloud",
                "opensearch.password=test"
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed Vault", e);
        }
    }

    /**
     * Injects dynamic container ports and credentials into Spring properties
     * so the application context connects to the test containers instead of
     * local services.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.cloud.vault.host", vault::getHost);
        registry.add("spring.cloud.vault.port", () -> vault.getMappedPort(8200));
        registry.add("spring.cloud.vault.scheme", () -> "http");
        registry.add("spring.cloud.vault.authentication", () -> "TOKEN");
        registry.add("spring.cloud.vault.token", () -> "test-root-token");
    }
}

package ee.taltech.ite4120.landuse;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * THE BUSINESS TESTS — the executable form of the plain-language tests in
 * docs/specifications/LANDUSE.01 § Business tests. Written before the
 * implementation; watched fail; then made green. If a rule here feels arbitrary,
 * read the specification — the rule is quoted from it.
 *
 * <p>Driven through HTTP, because the specification states the refusals as API
 * answers (400, {@code application/problem+json}). Runs against a real PostgreSQL
 * via Testcontainers, so the whole changelog (core + animals + landuse) is applied
 * from empty as well.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LandUseBusinessRulesIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @Value("${local.server.port}")
    int port;

    final HttpClient http = HttpClient.newHttpClient();

    @Test
    void aCadastralNumberMustHaveTheEstonianFormat() throws Exception {
        // Given the input 78401-101-0123, when the owner checks it
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/api/landuse/plots/78401-101-0123/restrictions"))
                .GET());

        // Then the check is refused as a client error, with the format spelled out
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                ct -> assertThat(ct).startsWith("application/problem+json"));
        assertThat(response.body()).contains("cadastral number must have the format NNNNN:NNN:NNNN");
    }

    @Test
    void aRuleCannotEndBeforeItStarts() throws Exception {
        // Given a rule valid from 2026-10-01, when it is saved valid until 2026-09-01
        String rule = """
                {
                  "restrictionTypeCode": "POWER_LINE_ZONE",
                  "forbidden": "Erecting buildings inside the protection zone.",
                  "allowedWithConsent": "Earthworks with the written consent of the network owner.",
                  "consentParty": "Owner of the power line",
                  "legalBasis": "Ehitusseadustik, ehitise kaitsevöönd",
                  "validFrom": "2026-10-01",
                  "validUntil": "2026-09-01"
                }
                """;
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/api/landuse/rules"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(rule)));

        // Then it is refused as a client error, naming the broken rule
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                ct -> assertThat(ct).startsWith("application/problem+json"));
        assertThat(response.body()).contains("valid until must not be before valid from");
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        // Mock auth: the Bearer token value is the username
        return http.send(request.header("Authorization", "Bearer alice").build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}

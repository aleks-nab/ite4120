package ee.taltech.ite4120.landuse;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
 *
 * <p>The check goes through the real HTTP adapter to the imitated register
 * ({@code /mock-registry/kpois}) served by this same application, so the port is
 * chosen up front: the adapter's URL is fixed when the context starts, before a
 * random port would be known.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class LandUseBusinessRulesIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    static final int port = freePort();

    @DynamicPropertySource
    static void imitatedRegister(DynamicPropertyRegistry registry) {
        registry.add("server.port", () -> port);
        registry.add("landuse.restriction-register.mock-server", () -> "true");
        registry.add("landuse.restriction-register.url", () -> "http://localhost:" + port + "/mock-registry/kpois");
    }

    final HttpClient http = HttpClient.newHttpClient();
    final ObjectMapper json = new ObjectMapper();

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

    @Test
    void theRegisterBeingDownNeverProducesAPartialAnswer() throws Exception {
        // Given the restrictions register does not answer (the imitated register fails 99999:999:9999),
        // when the owner checks the plot
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/api/landuse/plots/99999:999:9999/restrictions"))
                .GET());

        // Then the answer is 502 "register unavailable, try again", and no restrictions are listed
        assertThat(response.statusCode()).isEqualTo(502);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                ct -> assertThat(ct).startsWith("application/problem+json"));
        JsonNode problem = json.readTree(response.body());
        assertThat(problem.path("detail").asText()).isEqualTo("register unavailable, try again");
        assertThat(problem.has("restrictions")).isFalse();
    }

    @Test
    void aPlotTheRegisterDoesNotKnowIsNotFound() throws Exception {
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/api/landuse/plots/12345:123:1234/restrictions"))
                .GET());

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                ct -> assertThat(ct).startsWith("application/problem+json"));
    }

    @Test
    void aRuleNeedsWhatIsForbiddenAndItsLegalBasis() throws Exception {
        String rule = """
                {
                  "restrictionTypeCode": "POWER_LINE_ZONE",
                  "validFrom": "2026-10-01"
                }
                """;
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/api/landuse/rules"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(rule)));

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                ct -> assertThat(ct).startsWith("application/problem+json"));
    }

    @Test
    void aRetiredRuleIsNotFound() throws Exception {
        // Given a rule that exists (a type the other tests leave alone)
        String rule = """
                {
                  "restrictionTypeCode": "GAS_PIPELINE_ZONE",
                  "forbidden": "Building above the gas pipeline.",
                  "legalBasis": "Ehitusseadustik, ehitise kaitsevöönd",
                  "validFrom": "2026-01-01"
                }
                """;
        HttpResponse<String> created = send(HttpRequest.newBuilder(uri("/api/landuse/rules"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(rule)));
        assertThat(created.statusCode()).isEqualTo(201);
        String location = created.headers().firstValue("Location").orElseThrow();

        // When it is retired
        HttpResponse<String> retired = send(HttpRequest.newBuilder(uri(location)).DELETE());
        assertThat(retired.statusCode()).isEqualTo(204);

        // Then it is no longer found
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri(location)).GET());
        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void aPowerLineZoneIsExplainedByItsRule() throws Exception {
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/api/landuse/plots/78401:101:0123/restrictions"))
                .GET());

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode check = json.readTree(response.body());
        assertThat(check.path("cadastralCode").asText()).isEqualTo("78401:101:0123");
        JsonNode powerLine = null;
        for (JsonNode r : check.path("restrictions")) {
            if ("POWER_LINE_ZONE".equals(r.path("restrictionTypeCode").asText())) {
                powerLine = r;
            }
        }
        assertThat(powerLine).as("power line zone in the check").isNotNull();
        assertThat(powerLine.path("rule").isObject()).as("rule attached").isTrue();
        assertThat(powerLine.path("rule").path("restrictionTypeCode").asText()).isEqualTo("POWER_LINE_ZONE");
        assertThat(powerLine.path("rule").path("forbidden").asText()).isNotBlank();
    }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("no free port for the test server", e);
        }
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

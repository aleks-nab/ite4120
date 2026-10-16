package ee.taltech.ite4120.landuse.register.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.ite4120.landuse.register.RegisterPlot;
import ee.taltech.ite4120.landuse.register.RestrictionRegisterAdapter;
import ee.taltech.ite4120.landuse.register.RestrictionRegisterException;
import ee.taltech.ite4120.landuse.register.http.wire.KyKitsendusedResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Layer A: fetches the register's answer over plain HTTPS+JSON; {@link KpoisMapper}
 * translates the wire record into our {@link RegisterPlot}.
 *
 * <p>In the local configuration the URL points back at this application's
 * {@code /mock-registry/kpois} endpoint — an imitated environment.
 */
public class HttpRestrictionRegisterAdapter implements RestrictionRegisterAdapter {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    /* A register may add fields at any time; unknown ones must not break the check. */
    private final ObjectMapper json = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String baseUrl;

    public HttpRestrictionRegisterAdapter(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public RegisterPlot restrictions(String cadastralCode) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/restrictions/" + URLEncoder.encode(cadastralCode, StandardCharsets.UTF_8)))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                // A plot the register does not know is a MISS, not a failure.
                return null;
            }
            if (response.statusCode() != 200) {
                throw new RestrictionRegisterException(
                        "restrictions register answered HTTP " + response.statusCode());
            }
            return KpoisMapper.toPlot(json.readValue(response.body(), KyKitsendusedResponse.class));
        } catch (IOException e) {
            throw new RestrictionRegisterException("restrictions register unreachable: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestrictionRegisterException("restrictions register call interrupted", e);
        }
    }
}

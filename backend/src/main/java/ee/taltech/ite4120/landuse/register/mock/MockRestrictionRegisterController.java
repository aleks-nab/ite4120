package ee.taltech.ite4120.landuse.register.mock;

import ee.taltech.ite4120.landuse.register.http.wire.KyKitsendus;
import ee.taltech.ite4120.landuse.register.http.wire.KyKitsendusedResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The imitated restrictions register (KPOIS {@code kyKitsendusedAvalik}) — canned
 * plots served by the application itself, so the check works offline. Enabled
 * only when {@code landuse.restriction-register.mock-server=true} (the local profile).
 *
 * <p>It answers in the REGISTER's vocabulary (the wire records), not ours, so the
 * adapter exercises the same mapping it would against the real thing. The plots:
 * <ul>
 *   <li>{@code 78401:101:0123} — the demo pre-check's plot: power line and local road zones;</li>
 *   <li>{@code 78401:102:0045} — a shore plot with hidden restrictions and a type we have no rule for;</li>
 *   <li>{@code 79501:001:0001} — a plot with no restrictions;</li>
 *   <li>{@code 99999:999:9999} — the register failing (503), to see the check fail closed.</li>
 * </ul>
 */
@RestController
@RequestMapping("/mock-registry/kpois")
@ConditionalOnProperty(name = "landuse.restriction-register.mock-server", havingValue = "true")
public class MockRestrictionRegisterController {

    static final String FAILING_PLOT = "99999:999:9999";

    private static final Map<String, KyKitsendusedResponse> PLOTS = Map.of(
            "78401:101:0123", new KyKitsendusedResponse("78401:101:0123", new BigDecimal("1250.00"), false, List.of(
                    new KyKitsendus("ELEKTRIPAIGALDISE_KAITSEVOOND", "10 kV elektriliini kaitsevöönd",
                            "Ehitusseadustik", "Elektrilevi OÜ", new BigDecimal("187.50")),
                    new KyKitsendus("TEE_KAITSEVOOND", "Kohaliku tee kaitsevöönd",
                            "Ehitusseadustik", "Vallavalitsus", new BigDecimal("96.00")))),
            "78401:102:0045", new KyKitsendusedResponse("78401:102:0045", new BigDecimal("3400.00"), true, List.of(
                    new KyKitsendus("RANNA_EHITUSKEELUVOOND", "Järve ranna ehituskeeluvöönd",
                            "Looduskaitseseadus § 38", "Keskkonnaamet", new BigDecimal("1200.00")),
                    new KyKitsendus("MAAPARANDUSSUSTEEMI_KAITSEVOOND", "Maaparandussüsteemi kaitsevöönd",
                            "Maaparandusseadus", "Põllumajandus- ja Toiduamet", new BigDecimal("310.00")))),
            "79501:001:0001", new KyKitsendusedResponse("79501:001:0001", new BigDecimal("820.00"), false, List.of()));

    @GetMapping("/restrictions/{katastritunnus}")
    public ResponseEntity<KyKitsendusedResponse> restrictions(@PathVariable String katastritunnus) {
        if (FAILING_PLOT.equals(katastritunnus)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        KyKitsendusedResponse plot = PLOTS.get(katastritunnus);
        return plot == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(plot);
    }
}

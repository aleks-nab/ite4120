package ee.taltech.ite4120.landuse.register.http;

import ee.taltech.ite4120.landuse.register.RegisterPlot;
import ee.taltech.ite4120.landuse.register.RegisterRestriction;
import ee.taltech.ite4120.landuse.register.http.wire.KyKitsendus;
import ee.taltech.ite4120.landuse.register.http.wire.KyKitsendusedResponse;
import java.util.List;
import java.util.Map;

/**
 * The mapper — the ONLY place where the register's Estonian wire names become our
 * names, and where the register's restriction types become our
 * {@code landuse.restriction_type} codes.
 */
public final class KpoisMapper {

    /** Register type ({@code nahtuseLiik}) → our restriction type code. */
    static final Map<String, String> TYPES = Map.of(
            "ELEKTRIPAIGALDISE_KAITSEVOOND", "POWER_LINE_ZONE",
            "GAASIPAIGALDISE_KAITSEVOOND", "GAS_PIPELINE_ZONE",
            "TEE_KAITSEVOOND", "ROAD_PROTECTION_ZONE",
            "RANNA_EHITUSKEELUVOOND", "SHORE_BUILDING_EXCLUSION",
            "MALESTISE_KAITSEVOOND", "HERITAGE_PROTECTION",
            "LOODUSKAITSELINE_PIIRANG", "NATURE_PROTECTION");

    private KpoisMapper() {}

    public static RegisterPlot toPlot(KyKitsendusedResponse wire) {
        List<RegisterRestriction> restrictions = wire.kitsendused() == null ? List.of()
                : wire.kitsendused().stream().map(KpoisMapper::toRestriction).toList();
        return new RegisterPlot(wire.katastritunnus(), wire.pindala(), !wire.varjatudKitsendusi(), restrictions);
    }

    /** An unknown register type maps to no code: shown with its own name and basis, no rule. */
    static RegisterRestriction toRestriction(KyKitsendus k) {
        return new RegisterRestriction(TYPES.get(k.nahtuseLiik()), k.nimi(), k.kpoOmanik(), k.seaduslikAlus(), k.pindala());
    }
}

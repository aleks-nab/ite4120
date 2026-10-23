package ee.taltech.ite4120.landuse.register.http.wire;

import java.math.BigDecimal;
import java.util.List;

/**
 * WIRE RECORD — the answer of KPOIS {@code kyKitsendusedAvalik} (public
 * restrictions of a cadastral unit), exactly as the register names it.
 *
 * <p>Field names are the provider's Estonian names, deliberately NOT translated:
 * a wire record must stay diffable against the provider's schema. The English
 * renaming happens in exactly one place — {@code KpoisMapper}.
 *
 * @param katastritunnus     cadastral code of the unit
 * @param pindala            area of the cadastral unit, m²
 * @param varjatudKitsendusi true when the unit has restrictions the public service does not disclose
 * @param kitsendused        the disclosed restrictions
 */
public record KyKitsendusedResponse(
        String katastritunnus,
        BigDecimal pindala,
        boolean varjatudKitsendusi,
        List<KyKitsendus> kitsendused) {}

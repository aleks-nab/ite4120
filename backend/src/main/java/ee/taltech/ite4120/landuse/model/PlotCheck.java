package ee.taltech.ite4120.landuse.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * The answer of the restriction check: what the register says about a plot, each
 * restriction explained by the rule valid today. Not stored — the register owns
 * the restrictions; only a saved pre-check keeps a snapshot.
 *
 * @param registerComplete false when the register reported restrictions it does
 *        not disclose publicly — the list shown is then not the whole truth
 */
public record PlotCheck(
        String cadastralCode,
        BigDecimal plotAreaM2,
        boolean registerComplete,
        List<PlotRestriction> restrictions) {}

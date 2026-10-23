package ee.taltech.ite4120.landuse.register;

import java.math.BigDecimal;
import java.util.List;

/**
 * A plot as the register describes it, in OUR domain vocabulary — English names,
 * our restriction type codes. The register's own field names live in the wire
 * records and never leak past {@code KpoisMapper}.
 *
 * @param complete false when the register reported restrictions it does not
 *        disclose publicly — the owner must then be told the list is not complete
 */
public record RegisterPlot(
        String cadastralCode,
        BigDecimal plotAreaM2,
        boolean complete,
        List<RegisterRestriction> restrictions) {}

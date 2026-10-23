package ee.taltech.ite4120.landuse.register;

import java.math.BigDecimal;

/**
 * One restriction on a plot, in our vocabulary.
 *
 * @param restrictionTypeCode our {@code landuse.restriction_type.code}, or
 *        {@code null} when the register type has no counterpart in our list — the
 *        restriction is then shown with its own name and legal basis and no rule
 */
public record RegisterRestriction(
        String restrictionTypeCode,
        String restrictionName,
        String restrictingObjectOwner,
        String legalBasis,
        BigDecimal affectedAreaM2) {}

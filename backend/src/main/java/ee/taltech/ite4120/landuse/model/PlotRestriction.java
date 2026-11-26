package ee.taltech.ite4120.landuse.model;

import java.math.BigDecimal;

/**
 * One restriction found on a plot, as the register returned it, joined with the
 * rule valid today for its type. Field names follow
 * {@code landuse.pre_check_restriction}, which stores exactly this as a snapshot.
 *
 * @param restrictionTypeCode our type code; {@code null} for a register type we do not know
 * @param rule the rule valid today for the type; {@code null} when there is none
 */
public record PlotRestriction(
        String restrictionTypeCode,
        String restrictionName,
        String restrictingObjectOwner,
        String legalBasis,
        BigDecimal affectedAreaM2,
        RestrictionRule rule) {}

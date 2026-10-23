package ee.taltech.ite4120.landuse.api;

import ee.taltech.ite4120.landuse.model.RestrictionRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * The create/update request body for a rule. Bean Validation checks the *shape*
 * here at the edge; the rules that compare fields or need the database (validity
 * period, known type) live in RestrictionRuleService.
 */
public class RestrictionRuleRequest {

    @NotBlank
    private String restrictionTypeCode;

    @NotBlank
    @Size(max = 2000)
    private String forbidden;

    @Size(max = 2000)
    private String allowedWithConsent;

    @Size(max = 255)
    private String consentParty;

    @NotBlank
    @Size(max = 500)
    private String legalBasis;

    @NotNull
    private LocalDate validFrom;

    private LocalDate validUntil;

    public RestrictionRule toModel() {
        RestrictionRule r = new RestrictionRule();
        r.setRestrictionTypeCode(restrictionTypeCode);
        r.setForbidden(forbidden);
        r.setAllowedWithConsent(allowedWithConsent);
        r.setConsentParty(consentParty);
        r.setLegalBasis(legalBasis);
        r.setValidFrom(validFrom);
        r.setValidUntil(validUntil);
        return r;
    }

    public String getRestrictionTypeCode() { return restrictionTypeCode; }
    public void setRestrictionTypeCode(String restrictionTypeCode) { this.restrictionTypeCode = restrictionTypeCode; }
    public String getForbidden() { return forbidden; }
    public void setForbidden(String forbidden) { this.forbidden = forbidden; }
    public String getAllowedWithConsent() { return allowedWithConsent; }
    public void setAllowedWithConsent(String allowedWithConsent) { this.allowedWithConsent = allowedWithConsent; }
    public String getConsentParty() { return consentParty; }
    public void setConsentParty(String consentParty) { this.consentParty = consentParty; }
    public String getLegalBasis() { return legalBasis; }
    public void setLegalBasis(String legalBasis) { this.legalBasis = legalBasis; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
}

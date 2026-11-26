package ee.taltech.ite4120.landuse.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * One rule of the rule catalogue: what a restriction type means for building, in
 * plain language, over a validity period. Maps 1:1 onto
 * {@code landuse.restriction_rule} — see 02-restriction-rule.sql.
 *
 * <p>The {@code sys*} fields are filled by the shared {@code core.sys_columns()}
 * trigger, never by application code.
 */
public class RestrictionRule {

    private Long id;
    /** FK → landuse.restriction_type(code). */
    private String restrictionTypeCode;
    private String forbidden;
    private String allowedWithConsent;
    private String consentParty;
    private String legalBasis;
    private LocalDate validFrom;
    /** Optional; not before {@code validFrom}. */
    private LocalDate validUntil;

    private String sysStatus;
    private Long sysVersion;
    private OffsetDateTime sysCreatedAt;
    private String sysCreatedBy;
    private OffsetDateTime sysModifiedAt;
    private String sysModifiedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getSysStatus() { return sysStatus; }
    public void setSysStatus(String sysStatus) { this.sysStatus = sysStatus; }
    public Long getSysVersion() { return sysVersion; }
    public void setSysVersion(Long sysVersion) { this.sysVersion = sysVersion; }
    public OffsetDateTime getSysCreatedAt() { return sysCreatedAt; }
    public void setSysCreatedAt(OffsetDateTime sysCreatedAt) { this.sysCreatedAt = sysCreatedAt; }
    public String getSysCreatedBy() { return sysCreatedBy; }
    public void setSysCreatedBy(String sysCreatedBy) { this.sysCreatedBy = sysCreatedBy; }
    public OffsetDateTime getSysModifiedAt() { return sysModifiedAt; }
    public void setSysModifiedAt(OffsetDateTime sysModifiedAt) { this.sysModifiedAt = sysModifiedAt; }
    public String getSysModifiedBy() { return sysModifiedBy; }
    public void setSysModifiedBy(String sysModifiedBy) { this.sysModifiedBy = sysModifiedBy; }
}

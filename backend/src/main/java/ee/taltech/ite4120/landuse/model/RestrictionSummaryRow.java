package ee.taltech.ite4120.landuse.model;

import java.time.LocalDate;

/**
 * One row of the coverage report — one per restriction type, whether or not it
 * has a rule or any pre-checks. Zeros and nulls are answers, not missing data.
 */
public class RestrictionSummaryRow {

    private String typeCode;
    private String typeName;
    /** True when a rule for this type is valid today. */
    private Boolean ruleInForce;
    /** Consent party of the rule in force; null without one. */
    private String consentParty;
    /** Pre-checks in the period that showed this type. */
    private Long preCheckCount;
    /** Day of the latest such pre-check; null when there were none. */
    private LocalDate latestPreCheck;

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public Boolean getRuleInForce() { return ruleInForce; }
    public void setRuleInForce(Boolean ruleInForce) { this.ruleInForce = ruleInForce; }
    public String getConsentParty() { return consentParty; }
    public void setConsentParty(String consentParty) { this.consentParty = consentParty; }
    public Long getPreCheckCount() { return preCheckCount; }
    public void setPreCheckCount(Long preCheckCount) { this.preCheckCount = preCheckCount; }
    public LocalDate getLatestPreCheck() { return latestPreCheck; }
    public void setLatestPreCheck(LocalDate latestPreCheck) { this.latestPreCheck = latestPreCheck; }
}

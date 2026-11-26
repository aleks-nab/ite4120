package ee.taltech.ite4120.landuse.dto;

import java.time.LocalDate;
import org.helex.commons.model.QueryParams;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Query parameters for the rule list endpoint. Extends the platform
 * {@link QueryParams}, which carries {@code limit}, {@code offset} and {@code sort}.
 */
public class RuleQueryParams extends QueryParams {

    /** Exact restriction type filter, e.g. {@code POWER_LINE_ZONE}. */
    private String typeCode;
    /** Only rules valid on this day ({@code YYYY-MM-DD}). */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validOn;

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public LocalDate getValidOn() { return validOn; }
    public void setValidOn(LocalDate validOn) { this.validOn = validOn; }
}

package ee.taltech.ite4120.landuse.model;

/** Reference row from {@code landuse.restriction_type} — the lookup table of 01-restriction-type.sql. */
public class RestrictionType {

    private String code;
    private String name;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

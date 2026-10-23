package ee.taltech.ite4120.landuse.repository;

import ee.taltech.ite4120.landuse.dto.RuleQueryParams;
import ee.taltech.ite4120.landuse.model.RestrictionRule;
import ee.taltech.ite4120.landuse.model.RestrictionType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.helex.commons.db.bean.PgBeanProcessor;
import org.helex.commons.db.repo.BaseRepository;
import org.helex.commons.db.sql.SqlBuilder;
import org.helex.commons.exception.ApiClientException;
import org.helex.commons.model.QueryResult;
import org.springframework.stereotype.Repository;

/**
 * Data access for the rule catalogue and the restriction type list — the same
 * {@link BaseRepository} + {@link PgBeanProcessor} + {@link SqlBuilder} idiom as
 * {@code AnimalRepository}.
 */
@Repository
public class RestrictionRuleRepository extends BaseRepository {

    private static final String SELECT = "select r.* from landuse.restriction_rule r ";

    /** The sort keys the list endpoint accepts (`sort=validFrom`, `sort=-validFrom`) and their columns. */
    private static final Map<String, String> SORT = Map.of(
            "restrictionTypeCode", "r.restriction_type_code",
            "validFrom", "r.valid_from",
            "validUntil", "r.valid_until");

    private final PgBeanProcessor bp = new PgBeanProcessor(RestrictionRule.class);
    private final PgBeanProcessor typeBp = new PgBeanProcessor(RestrictionType.class);

    public RestrictionRule load(Long id) {
        return getBean(SELECT + "where r.id = ? and r.sys_status = 'A'", bp, id);
    }

    /**
     * The rule valid on {@code day} for a type. Should periods ever overlap, the
     * rule that started latest wins — it is the newer statement of the same rule.
     */
    public RestrictionRule validOn(String typeCode, LocalDate day) {
        return getBean(SELECT + """
                where r.restriction_type_code = ? and r.sys_status = 'A'
                  and r.valid_from <= ? and (r.valid_until is null or r.valid_until >= ?)
                order by r.valid_from desc, r.id desc
                limit 1""", bp, typeCode, day, day);
    }

    public QueryResult<RestrictionRule> search(RuleQueryParams p) {
        return query(p, this::count, this::list);
    }

    private Integer count(RuleQueryParams p) {
        SqlBuilder sb = new SqlBuilder("select count(1) from landuse.restriction_rule r where r.sys_status = 'A'");
        sb.append(filter(p));
        return queryForObject(sb.getSql(), Integer.class, sb.getParams());
    }

    private List<RestrictionRule> list(RuleQueryParams p) {
        SqlBuilder sb = new SqlBuilder(SELECT + "where r.sys_status = 'A'");
        sb.append(filter(p));
        sb.append(order(p));
        sb.append(limit(p));
        return getBeans(sb.getSql(), bp, sb.getParams());
    }

    /** A mistyped {@code ?sort=} is the caller's fault: 400 with the allowed keys, not 500. */
    private SqlBuilder order(RuleQueryParams p) {
        try {
            return order(p, SORT);
        } catch (IllegalArgumentException e) {
            throw new ApiClientException("unknown sort key; sortable: " + String.join(", ", SORT.keySet().stream().sorted().toList()));
        }
    }

    private SqlBuilder filter(RuleQueryParams p) {
        SqlBuilder sb = new SqlBuilder();
        sb.appendIfNotNull("and r.restriction_type_code = ?", p.getTypeCode());
        if (p.getValidOn() != null) {
            sb.and("(r.valid_from <= ? and (r.valid_until is null or r.valid_until >= ?))", p.getValidOn(), p.getValidOn());
        }
        return sb;
    }

    public Long insert(RestrictionRule r) {
        String sql = """
                insert into landuse.restriction_rule (restriction_type_code, forbidden, allowed_with_consent,
                                                      consent_party, legal_basis, valid_from, valid_until)
                values (?, ?, ?, ?, ?, ?, ?)
                returning id""";
        return queryForObject(sql, Long.class,
                r.getRestrictionTypeCode(), r.getForbidden(), r.getAllowedWithConsent(),
                r.getConsentParty(), r.getLegalBasis(), r.getValidFrom(), r.getValidUntil());
    }

    public boolean update(RestrictionRule r) {
        String sql = """
                update landuse.restriction_rule
                   set restriction_type_code = ?, forbidden = ?, allowed_with_consent = ?, consent_party = ?,
                       legal_basis = ?, valid_from = ?, valid_until = ?
                 where id = ? and sys_status = 'A'""";
        return jdbcTemplate.update(sql, r.getRestrictionTypeCode(), r.getForbidden(), r.getAllowedWithConsent(),
                r.getConsentParty(), r.getLegalBasis(), r.getValidFrom(), r.getValidUntil(), r.getId()) == 1;
    }

    /** Soft delete — a retired rule stays readable for the pre-checks that showed it. */
    public boolean retire(Long id) {
        return jdbcTemplate.update(
                "update landuse.restriction_rule set sys_status = 'C' where id = ? and sys_status = 'A'", id) == 1;
    }

    public List<RestrictionType> typeList() {
        return getBeans("select t.* from landuse.restriction_type t order by t.name", typeBp);
    }

    public boolean typeExists(String code) {
        Integer n = queryForObject("select count(1) from landuse.restriction_type where code = ?", Integer.class, code);
        return n != null && n > 0;
    }
}

package ee.taltech.ite4120.landuse.repository;

import ee.taltech.ite4120.landuse.model.RestrictionSummaryRow;
import java.time.LocalDate;
import java.util.List;
import org.helex.commons.db.bean.PgBeanProcessor;
import org.helex.commons.db.repo.BaseRepository;
import org.springframework.stereotype.Repository;

/** The coverage report — one SQL query over all four landuse tables. */
@Repository
public class ReportRepository extends BaseRepository {

    /*
     * Driven by restriction_type with LEFT JOINs only, so every type appears even
     * with no rule and no pre-checks (then: rule_in_force false, count 0, nulls).
     * The rule in force is picked as in RestrictionRuleRepository.validOn — valid
     * today, latest start wins — via a lateral join, so it can never double a row.
     * The period filter sits in the pre_check join condition, not in WHERE, which
     * would turn the outer join back into an inner one and drop the empty types.
     * A pre-check is dated by its sys_created_at (the day it was saved).
     */
    private static final String SUMMARY = """
            select t.code                          as type_code,
                   t.name                          as type_name,
                   (rr.id is not null)             as rule_in_force,
                   rr.consent_party                as consent_party,
                   count(distinct pc.id)           as pre_check_count,
                   max(pc.sys_created_at)::date    as latest_pre_check
              from landuse.restriction_type t
              left join lateral (
                       select r.id, r.consent_party
                         from landuse.restriction_rule r
                        where r.restriction_type_code = t.code and r.sys_status = 'A'
                          and r.valid_from <= current_date
                          and (r.valid_until is null or r.valid_until >= current_date)
                        order by r.valid_from desc, r.id desc
                        limit 1) rr on true
              left join landuse.pre_check_restriction pcr
                     on pcr.restriction_type_code = t.code and pcr.sys_status = 'A'
              left join landuse.pre_check pc
                     on pc.id = pcr.pre_check_id and pc.sys_status = 'A'
                    and pc.sys_created_at::date between ? and ?
             group by t.code, t.name, rr.id, rr.consent_party
             order by t.name""";

    private final PgBeanProcessor bp = new PgBeanProcessor(RestrictionSummaryRow.class);

    public List<RestrictionSummaryRow> restrictionSummary(LocalDate from, LocalDate to) {
        return getBeans(SUMMARY, bp, from, to);
    }
}

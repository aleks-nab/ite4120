package ee.taltech.ite4120.landuse.service;

import ee.taltech.ite4120.landuse.dto.RuleQueryParams;
import ee.taltech.ite4120.landuse.model.RestrictionRule;
import ee.taltech.ite4120.landuse.model.RestrictionType;
import ee.taltech.ite4120.landuse.repository.RestrictionRuleRepository;
import java.util.List;
import org.helex.commons.exception.ApiClientException;
import org.helex.commons.exception.NotFoundException;
import org.helex.commons.model.QueryResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business rules for the rule catalogue — the ones written as business tests in
 * docs/specifications/LANDUSE.01.
 */
@Service
public class RestrictionRuleService {

    private final RestrictionRuleRepository repository;

    public RestrictionRuleService(RestrictionRuleRepository repository) {
        this.repository = repository;
    }

    public QueryResult<RestrictionRule> search(RuleQueryParams params) {
        return repository.search(params);
    }

    public RestrictionRule require(Long id) {
        RestrictionRule r = repository.load(id);
        if (r == null) {
            throw new NotFoundException("restriction rule " + id + " not found");
        }
        return r;
    }

    @Transactional
    public RestrictionRule create(RestrictionRule rule) {
        validate(rule);
        Long id = repository.insert(rule);
        return require(id);
    }

    @Transactional
    public RestrictionRule update(Long id, RestrictionRule rule) {
        require(id);
        validate(rule);
        rule.setId(id);
        if (!repository.update(rule)) {
            throw new NotFoundException("restriction rule " + id + " not found");
        }
        return require(id);
    }

    @Transactional
    public void retire(Long id) {
        if (!repository.retire(id)) {
            throw new NotFoundException("restriction rule " + id + " not found");
        }
    }

    public List<RestrictionType> types() {
        return repository.typeList();
    }

    private void validate(RestrictionRule r) {
        // Business rule: a rule cannot end before it starts.
        if (r.getValidUntil() != null && r.getValidUntil().isBefore(r.getValidFrom())) {
            throw new ApiClientException("valid until must not be before valid from");
        }
        // Referential rule surfaced as a clear 400 rather than a raw FK violation.
        if (!repository.typeExists(r.getRestrictionTypeCode())) {
            throw new ApiClientException("unknown restrictionTypeCode: " + r.getRestrictionTypeCode());
        }
    }
}

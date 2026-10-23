package ee.taltech.ite4120.landuse.service;

import ee.taltech.ite4120.landuse.model.PlotCheck;
import ee.taltech.ite4120.landuse.model.PlotRestriction;
import ee.taltech.ite4120.landuse.register.RegisterPlot;
import ee.taltech.ite4120.landuse.register.RegisterRestriction;
import ee.taltech.ite4120.landuse.register.RestrictionRegisterAdapter;
import ee.taltech.ite4120.landuse.repository.RestrictionRuleRepository;
import java.time.LocalDate;
import java.util.regex.Pattern;
import org.helex.commons.exception.ApiClientException;
import org.helex.commons.exception.NotFoundException;
import org.springframework.stereotype.Service;

/**
 * The restriction check (LANDUSE.01): the restrictions come from the register,
 * the explanations from our rule catalogue.
 *
 * <p>Fails CLOSED: when the register cannot answer, the
 * {@link ee.taltech.ite4120.landuse.register.RestrictionRegisterException} is
 * left to propagate (502) — a partial list of restrictions looks complete and
 * could mislead an owner into building.
 */
@Service
public class PlotCheckService {

    /** Estonian cadastral code — an identifier, a string, never a number. */
    private static final Pattern CADASTRAL_CODE = Pattern.compile("\\d{5}:\\d{3}:\\d{4}");

    private final RestrictionRegisterAdapter register;
    private final RestrictionRuleRepository rules;

    public PlotCheckService(RestrictionRegisterAdapter register, RestrictionRuleRepository rules) {
        this.register = register;
        this.rules = rules;
    }

    public PlotCheck check(String cadastralCode) {
        // Business rule: a malformed code is refused before any register call.
        if (cadastralCode == null || !CADASTRAL_CODE.matcher(cadastralCode).matches()) {
            throw new ApiClientException("cadastral number must have the format NNNNN:NNN:NNNN");
        }
        RegisterPlot plot = register.restrictions(cadastralCode);
        if (plot == null) {
            throw new NotFoundException("plot " + cadastralCode + " is not known to the restrictions register");
        }
        LocalDate today = LocalDate.now();
        return new PlotCheck(cadastralCode, plot.plotAreaM2(), plot.complete(),
                plot.restrictions().stream().map(r -> explain(r, today)).toList());
    }

    /** Joins a register restriction with the rule valid today for its type, if any. */
    private PlotRestriction explain(RegisterRestriction r, LocalDate today) {
        return new PlotRestriction(r.restrictionTypeCode(), r.restrictionName(), r.restrictingObjectOwner(),
                r.legalBasis(), r.affectedAreaM2(),
                r.restrictionTypeCode() == null ? null : rules.validOn(r.restrictionTypeCode(), today));
    }
}

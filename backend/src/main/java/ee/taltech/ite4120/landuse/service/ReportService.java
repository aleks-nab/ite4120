package ee.taltech.ite4120.landuse.service;

import ee.taltech.ite4120.landuse.model.RestrictionSummaryRow;
import ee.taltech.ite4120.landuse.repository.ReportRepository;
import java.time.LocalDate;
import java.util.List;
import org.helex.commons.exception.ApiClientException;
import org.springframework.stereotype.Service;

/** The coverage report (LANDUSE.01 § API, reports). */
@Service
public class ReportService {

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public List<RestrictionSummaryRow> restrictionSummary(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new ApiClientException("to must not be before from");
        }
        return repository.restrictionSummary(from, to);
    }
}

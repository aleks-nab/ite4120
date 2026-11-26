package ee.taltech.ite4120.landuse.api;

import ee.taltech.ite4120.landuse.model.RestrictionSummaryRow;
import ee.taltech.ite4120.landuse.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Reports over the rule catalogue and the saved pre-checks (LANDUSE.01). */
@RestController
@RequestMapping("/api/landuse/reports")
@Tag(name = "Land use — reports", description = "Coverage of restriction types by rules and pre-checks")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @Operation(summary = "One row per restriction type: rule in force today, its consent party, "
            + "pre-checks in the period that showed the type and the latest one")
    @ApiResponse(responseCode = "200", description = "Every restriction type, zeros included")
    @ApiResponse(responseCode = "400", description = "to before from")
    @GetMapping("/restriction-summary")
    public List<RestrictionSummaryRow> restrictionSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.restrictionSummary(from, to);
    }
}

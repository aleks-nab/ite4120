package ee.taltech.ite4120.landuse.api;

import ee.taltech.ite4120.landuse.dto.RuleQueryParams;
import ee.taltech.ite4120.landuse.model.RestrictionRule;
import ee.taltech.ite4120.landuse.model.RestrictionType;
import ee.taltech.ite4120.landuse.service.RestrictionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.helex.commons.model.QueryResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The rule catalogue and the restriction type list (LANDUSE.01). */
@RestController
@RequestMapping("/api/landuse")
@Tag(name = "Land use — rules", description = "The rule catalogue that explains each restriction type")
public class RestrictionRuleController {

    private final RestrictionRuleService service;

    public RestrictionRuleController(RestrictionRuleService service) {
        this.service = service;
    }

    @Operation(summary = "Restriction type reference list")
    @GetMapping("/restriction-types")
    public List<RestrictionType> types() {
        return service.types();
    }

    @Operation(summary = "Search rules — paginated, filterable by type and validity day, sortable")
    @GetMapping("/rules")
    public QueryResult<RestrictionRule> search(@ModelAttribute RuleQueryParams params) {
        return service.search(params);
    }

    @Operation(summary = "One active rule by id")
    @ApiResponse(responseCode = "200", description = "The rule")
    @ApiResponse(responseCode = "404", description = "No active rule with that id")
    @GetMapping("/rules/{id}")
    public RestrictionRule byId(@PathVariable Long id) {
        return service.require(id);
    }

    @Operation(summary = "Add a rule")
    @ApiResponse(responseCode = "201", description = "Created — Location header points at the new rule")
    @ApiResponse(responseCode = "400", description = "Validation failed (unknown type, validUntil before validFrom…)")
    @PostMapping("/rules")
    public ResponseEntity<RestrictionRule> create(@Valid @RequestBody RestrictionRuleRequest body) {
        RestrictionRule saved = service.create(body.toModel());
        return ResponseEntity.created(URI.create("/api/landuse/rules/" + saved.getId())).body(saved);
    }

    @Operation(summary = "Update a rule")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "No active rule with that id")
    @PutMapping("/rules/{id}")
    public RestrictionRule update(@PathVariable Long id, @Valid @RequestBody RestrictionRuleRequest body) {
        return service.update(id, body.toModel());
    }

    @Operation(summary = "Retire a rule (soft delete)")
    @ApiResponse(responseCode = "204", description = "Retired")
    @ApiResponse(responseCode = "404", description = "No active rule with that id")
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        service.retire(id);
        return ResponseEntity.noContent().build();
    }
}

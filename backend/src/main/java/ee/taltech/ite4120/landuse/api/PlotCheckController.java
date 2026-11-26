package ee.taltech.ite4120.landuse.api;

import ee.taltech.ite4120.landuse.model.PlotCheck;
import ee.taltech.ite4120.landuse.service.PlotCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The land use restriction check (LANDUSE.01). */
@RestController
@RequestMapping("/api/landuse/plots")
@Tag(name = "Land use — check", description = "Restrictions on a plot, explained by the rule catalogue")
public class PlotCheckController {

    private final PlotCheckService service;

    public PlotCheckController(PlotCheckService service) {
        this.service = service;
    }

    @Operation(summary = "Restrictions on a plot, each with the rule valid today",
            description = "The restrictions come from the restrictions register (KPOIS) through the "
                    + "registry adapter; they are not stored. Fails closed: no partial answer when the "
                    + "register is down.")
    @ApiResponse(responseCode = "200", description = "The restrictions; registerComplete=false when the register hides some")
    @ApiResponse(responseCode = "400", description = "Malformed cadastral code — refused before any register call")
    @ApiResponse(responseCode = "404", description = "The register does not know the plot")
    @ApiResponse(responseCode = "502", description = "Register unavailable, try again")
    @GetMapping("/{cadastralCode}/restrictions")
    public PlotCheck restrictions(
            @Parameter(description = "Cadastral code NNNNN:NNN:NNNN", example = "78401:101:0123")
            @PathVariable String cadastralCode) {
        return service.check(cadastralCode);
    }
}

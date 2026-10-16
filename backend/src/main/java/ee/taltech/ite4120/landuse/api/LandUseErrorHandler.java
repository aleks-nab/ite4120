package ee.taltech.ite4120.landuse.api;

import ee.taltech.ite4120.landuse.register.RestrictionRegisterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * The landuse-specific error: the restrictions register being down. Everything
 * else (400, 404, 409) is the shared {@code ApiErrorHandler}'s job.
 */
@RestControllerAdvice
public class LandUseErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(LandUseErrorHandler.class);

    /**
     * An upstream failure, not the caller's fault: 502. The owner gets a plain
     * "try again" and no partial answer; the technical cause goes to the log.
     */
    @ExceptionHandler(RestrictionRegisterException.class)
    public ProblemDetail registerFailure(RestrictionRegisterException e) {
        log.warn("restrictions register failed: {}", e.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        pd.setTitle("Restrictions register unavailable");
        pd.setDetail("register unavailable, try again");
        return pd;
    }
}

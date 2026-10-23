package ee.taltech.ite4120.landuse.register;

import ee.taltech.ite4120.landuse.register.http.HttpRestrictionRegisterAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chooses the transport behind {@link RestrictionRegisterAdapter}. Plain HTTPS+JSON
 * against {@code landuse.restriction-register.url}; locally that URL points at this
 * very application's imitated register ({@code /mock-registry/kpois}). In production
 * the same wire records would travel over X-Road — a second adapter, chosen here.
 */
@Configuration
public class RestrictionRegisterConfig {

    @Bean
    public RestrictionRegisterAdapter restrictionRegisterAdapter(
            @Value("${landuse.restriction-register.url:http://localhost:18440/mock-registry/kpois}") String url) {
        return new HttpRestrictionRegisterAdapter(url);
    }
}

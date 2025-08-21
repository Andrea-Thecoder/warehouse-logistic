package it.warehouse.optimization.config;

import com.graphhopper.util.Translation;
import com.graphhopper.util.TranslationMap;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.util.Locale;

@ApplicationScoped
@Getter
public class RoutingConfig {

    @ConfigProperty(name = "warehouse.coordinates.validation.enabled", defaultValue = "true")
    boolean coordinatesValidationEnabled;

    @ConfigProperty(name = "warehouse.lat.min")
    BigDecimal minLat;

    @ConfigProperty(name = "warehouse.lat.max")
    BigDecimal maxLat;

    @ConfigProperty(name = "warehouse.lon.min")
    BigDecimal minLon;

    @ConfigProperty(name = "warehouse.lon.max")
    BigDecimal maxLon;

    @ConfigProperty(name = "warehouse.routing.language", defaultValue = "it")
    String routingLanguage;

    private Translation translation;

    @PostConstruct
    void initTranslation() {
        TranslationMap translationMap = new TranslationMap().doImport();
        Locale locale = Locale.forLanguageTag(routingLanguage);
        this.translation = translationMap.getWithFallBack(locale);
    }

}

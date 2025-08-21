package it.warehouse.optimization.utils;

import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.graphhopper.util.Translation;
import it.warehouse.optimization.config.RoutingConfig;
import it.warehouse.optimization.exception.ServiceException;
import it.warehouse.optimization.model.City;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@ApplicationScoped
@Slf4j

public class RoutingUtils {

    @Inject
    RoutingConfig routingConfig;


    public void validateCoordinates(City city) {
        if(!routingConfig.isCoordinatesValidationEnabled()) return;
        BigDecimal lat = city.getLatitude();
        BigDecimal lon = city.getLongitude();

        if(lat == null || lon == null){
            log.error("validateCoordinates: Latitude or longitude for city {} is null", city.getName());
            throw new ServiceException("City: " + city.getName() + " has null coordinates");
        }

        boolean isLatitudeOutOfRange = lat.compareTo(routingConfig.getMinLat()) < 0 ||
                lat.compareTo(routingConfig.getMaxLat()) > 0;
        boolean isLongitudeOutOfRange =  lon.compareTo(routingConfig.getMinLon()) < 0 ||
                lon.compareTo(routingConfig.getMaxLon()) > 0;

        if(isLatitudeOutOfRange || isLongitudeOutOfRange){
            log.error("validateCoordinates: Latitude or longitude for city {} is out of range", city.getName());
            throw new ServiceException("City '" + city.getName() + "' has coordinates outside the valid range");
        }
    }


    public String toWkt(PointList points) {
        StringBuilder sb = new StringBuilder("LINESTRING (");
        for(int i=0; i<points.size(); i++){
            double lat = points.getLat(i);
            double lon = points.getLon(i);
            if(i>0) sb.append(", ");
            sb.append(lon).append(" ").append(lat); // WKT usa lon lat
        }
        sb.append(")");
        return sb.toString();
    }

    public List<String> createInstructions(InstructionList instructionsList){
        if(CollectionUtils.isEmpty(instructionsList)) return null;
        return instructionsList.stream()
                .map(instruction -> instruction.getTurnDescription(routingConfig.getTranslation()))
                .toList();
    }

    public String getRoutingLanguage(){
        return routingConfig.getRoutingLanguage();
    }


    public static double metersInKilometers(BigDecimal meters){
        if(meters == null ) return 0;
        return meters
                .divide(BigDecimal.valueOf(1000), 10, RoundingMode.DOWN)
                .setScale(3, RoundingMode.DOWN)
                .doubleValue();
    }

    public static LocalDateTime  calculateEstimatedArrival(Date departure, BigDecimal estimatedDurationMillis){
        //TODO rendere parametrizzato il valore di ore di guida giornaliere(ora 9)
        long totalSeconds = Duration.ofMillis(estimatedDurationMillis.longValue()).getSeconds();

        long fullDays = totalSeconds / (9 * 3600);  // secondi in 9 ore
        long remainingSeconds = totalSeconds % (9 * 3600);

        long hours = remainingSeconds / 3600;
        remainingSeconds %= 3600;
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;

        return departure.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .plusDays(fullDays)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);
    }





}

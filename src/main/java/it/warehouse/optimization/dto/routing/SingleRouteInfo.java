package it.warehouse.optimization.dto.routing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SingleRouteInfo {

    protected BigDecimal distanceInMeters;
    protected BigDecimal timeInMillis;
    protected String geometry;
    protected LocalDateTime estimatedArrival;
    protected List<String> instructions;
   


}

package it.warehouse.optimization.dto.routing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class RouteInfo {

    private BigDecimal distanceInMeters;
    private BigDecimal timeInMillis;
    private String geometry;
    private List<String> instructions;


}

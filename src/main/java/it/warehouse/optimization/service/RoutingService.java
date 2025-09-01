package it.warehouse.optimization.service;


import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import it.warehouse.optimization.config.GraphHopperConfig;
import it.warehouse.optimization.dto.movementdestination.InsertMovementDestinationDTO;
import it.warehouse.optimization.dto.routing.RouteInfo;
import it.warehouse.optimization.model.City;
import it.warehouse.optimization.model.Warehouse;
import it.warehouse.optimization.utils.RoutingUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ApplicationScoped
@Slf4j

public class RoutingService {

    @Inject
    GraphHopperConfig graphHopperConfig;

    @Inject
    RoutingUtils routingUtils;


    public RouteInfo calculateRoute(City originCity, City destinationCity){
        log.info("calculateRoute: Starting calculate route from city: {} to city: {}", originCity.getName(),destinationCity.getName());
        routingUtils.validateCoordinates(originCity);
        routingUtils.validateCoordinates(destinationCity);

        GraphHopper hopper = graphHopperConfig.getHopper();


        GHRequest request = new GHRequest(
                originCity.getLatitude().doubleValue(),
                originCity.getLongitude().doubleValue(),
                destinationCity.getLatitude().doubleValue(),
                destinationCity.getLongitude().doubleValue())
                .setProfile("car")
                .setLocale(Locale.forLanguageTag(routingUtils.getRoutingLanguage()));

        GHResponse response = hopper.route(request);

        if (response.hasErrors()) {
            log.error("calculateRoute: Error while calculating route. Error message: {}", response.getErrors());
            throw new RuntimeException("Error calculating route: " + response.getErrors());
        }
        ResponsePath path = response.getBest();
        return createRouteInfo(path);
    }




    private List<GHPoint> createPoints(City originCity, Set<Warehouse> warehouses){
        matrix
        List<GHPoint> points = new ArrayList<>();
        points.add(new GHPoint(originCity.getLatitude(), originCity.getLongitude()));
        for (Warehouse wh : warehouses) {
            points.add(new GHPoint(wh.getCity().getLatitude(), wh.getCity().getLongitude()));
        }
        return points;
    }


    private RouteInfo createRouteInfo(ResponsePath path){
        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setDistanceInMeters(BigDecimal.valueOf(path.getDistance()));
        routeInfo.setTimeInMillis(BigDecimal.valueOf(path.getTime()));
        routeInfo.setGeometry(routingUtils.toWkt(path.getPoints()));
        routeInfo.setInstructions(routingUtils.createInstructions(path.getInstructions()));
        return routeInfo;
    }


}

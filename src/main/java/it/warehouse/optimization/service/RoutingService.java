package it.warehouse.optimization.service;


import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
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


    public RouteInfo calculateRoute(City originCity, City destinationCity) {
        log.info("calculateRoute: Starting calculate route from city: {} to city: {}", originCity.getName(), destinationCity.getName());
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

    public RouteInfo calculateRoute2(City originCity, Set<Warehouse> warehouses) {
        log.info("calculateRoute: Starting calculate route from city: {}", originCity.getName());

        List<GHPoint> points = new ArrayList<>();

        createPoints(originCity, warehouses, points);
        final int n = points.size();
        long[][] distanceMatrix = new long[n][n];
        long[][] timeMatrix = new long[n][n];

        GraphHopper hopper = graphHopperConfig.getHopper();

        populateMatrix(distanceMatrix, timeMatrix, points, hopper);

        RoutingIndexManager manager = new RoutingIndexManager(n, 1, 0);
        RoutingModel routing = new RoutingModel(manager);

        routing.setArcCostEvaluatorOfAllVehicles((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return distanceMatrix[fromNode][toNode];
        });

        Assignment solution = routing.solve();
        if (solution == null) {
            log.error("calculateMultiDropRoute: Unable to solve TSP");
            throw new RuntimeException("Cannot solve TSP for multi-drop route.");
        }

        List<RouteInfo> routeList = new ArrayList<>();
        long index = routing.start(0);
        int stopOrder = 1;

        long cumulativeTime = 0;
        long cumulativeDistance = 0;

        while (!routing.isEnd(index)) {
            int node = manager.indexToNode(index);
            if (node != 0) { // skip origine
                GHPoint point = points.get(node);

                RouteInfo ri = new RouteInfo();
                ri.setStopOrder(stopOrder++);
                ri.setDistanceInMeters(BigDecimal.valueOf(distanceMatrix[0][node])); // distanza dall'origine o cumulativa
                ri.setTimeInMillis(BigDecimal.valueOf(timeMatrix[0][node])); // tempo dall'origine
                // stimiamo arrivo cumulativo
                cumulativeTime += timeMatrix[0][node];
                cumulativeDistance += distanceMatrix[0][node];
                ri.setEstimatedArrival(routingUtils.calculateEstimatedArrival(/* startTime */ null, cumulativeTime));
                ri.setGeometry(""); // puoi opzionale calcolare geometria se vuoi
                routeList.add(ri);
            }
            index = solution.value(routing.nextVar(index));
        }

        return null;

    }


    private void createPoints(City originCity, Set<Warehouse> warehouses, List<GHPoint> points) {
        routingUtils.validateCoordinates(originCity);
        points.add(new GHPoint(originCity.getLatitude().doubleValue(), originCity.getLongitude().doubleValue()));
        for (Warehouse wh : warehouses) {
            routingUtils.validateCoordinates(wh.getCity());
            points.add(new GHPoint(wh.getCity().getLatitude().doubleValue(), wh.getCity().getLongitude().doubleValue()));
        }
    }

    private void populateMatrix(long[][] distanceMatrix, long[][] timeMatrix, List<GHPoint> points, GraphHopper hopper) {
        final int SIZE = points.size();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (i == j) continue;

                GHRequest req = new GHRequest(points.get(i), points.get(j))
                        .setProfile("car")
                        .setLocale(Locale.forLanguageTag(routingUtils.getRoutingLanguage()));

                GHResponse resp = hopper.route(req);

                if (resp.hasErrors()) {
                    log.error("calculateRoute: Error while calculating route. Error message: {}", resp.getErrors());
                    throw new RuntimeException("Error calculating route: " + resp.getErrors());
                }

                distanceMatrix[i][j] = Math.round(resp.getBest().getDistance());
                timeMatrix[i][j] = resp.getBest().getTime();
            }
        }
    }


    private RouteInfo createRouteInfo(ResponsePath path) {
        RouteInfo routeInfo = new RouteInfo();
        routeInfo.setDistanceInMeters(BigDecimal.valueOf(path.getDistance()));
        routeInfo.setTimeInMillis(BigDecimal.valueOf(path.getTime()));
        routeInfo.setGeometry(routingUtils.toWkt(path.getPoints()));
        routeInfo.setInstructions(routingUtils.createInstructions(path.getInstructions()));
        return routeInfo;
    }


}

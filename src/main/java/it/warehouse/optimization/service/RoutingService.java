package it.warehouse.optimization.service;


import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.main;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import it.warehouse.optimization.config.GraphHopperConfig;
import it.warehouse.optimization.dto.routing.MultiRouteInfo;
import it.warehouse.optimization.dto.routing.SingleRouteInfo;
import it.warehouse.optimization.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.optimization.model.City;
import it.warehouse.optimization.model.Warehouse;
import it.warehouse.optimization.utils.RoutingUtils;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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

    @PostConstruct
    void init() {
        Loader.loadNativeLibraries();
    }


    public SingleRouteInfo calculateRoute(City originCity, City destinationCity) {
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


    public List<MultiRouteInfo> calculateMultiDropRoute(Warehouse originWarehouse, Set<Warehouse> warehouses) {
        City originCity = originWarehouse.getCity();
        log.info("calculateMultiDropRoute: {} destinations from warehouse {}", warehouses.size(), originWarehouse.getName());

        GraphHopper hopper = graphHopperConfig.getHopper();
        routingUtils.validateCoordinates(originCity);

        List<Warehouse> warehouseList = new ArrayList<>(warehouses);
        for (Warehouse wh : warehouseList) {
            routingUtils.validateCoordinates(wh.getCity());
        }

        int n = warehouseList.size();

        // Raccoglie coordinate: indice 0 = origine, 1..n = magazzini
        double[] lats = new double[n + 1];
        double[] lons = new double[n + 1];
        lats[0] = originCity.getLatitude().doubleValue();
        lons[0] = originCity.getLongitude().doubleValue();
        for (int i = 0; i < n; i++) {
            lats[i + 1] = warehouseList.get(i).getCity().getLatitude().doubleValue();
            lons[i + 1] = warehouseList.get(i).getCity().getLongitude().doubleValue();
        }

        // Matrice Haversine N×N — zero chiamate esterne
        long[][] distanceMatrix = buildHaversineMatrix(lats, lons, n + 1);

        // OR-Tools TSP: trova l'ordine ottimale di visita
        List<Integer> optimalOrder = solveWithOrTools(distanceMatrix, n);

        // GraphHopper: N chiamate per geometria + istruzioni nell'ordine ottimale
        List<MultiRouteInfo> routeInfos = new ArrayList<>();
        double totalDistance = 0;
        long totalTime = 0;
        Date departure = new Date();

        double currentLat = lats[0];
        double currentLon = lons[0];
        Warehouse previousWarehouse = originWarehouse;

        for (int stopOrder = 1; stopOrder <= optimalOrder.size(); stopOrder++) {
            int whIndex = optimalOrder.get(stopOrder - 1);
            Warehouse nextWarehouse = warehouseList.get(whIndex);
            double nextLat = lats[whIndex + 1];
            double nextLon = lons[whIndex + 1];

            GHRequest req = new GHRequest(currentLat, currentLon, nextLat, nextLon)
                    .setProfile("car")
                    .setLocale(Locale.forLanguageTag(routingUtils.getRoutingLanguage()));

            GHResponse resp = hopper.route(req);
            if (resp.hasErrors()) {
                log.error("calculateMultiDropRoute: GraphHopper error at stop {}: {}", stopOrder, resp.getErrors());
                throw new RuntimeException("Error routing leg " + stopOrder + ": " + resp.getErrors());
            }

            ResponsePath path = resp.getBest();
            totalDistance += path.getDistance();
            totalTime += path.getTime();

            MultiRouteInfo ri = new MultiRouteInfo();
            ri.setStopOrder(stopOrder);
            ri.setFromWarehouse(SimpleDetailWarehouseDTO.of(previousWarehouse));
            ri.setToWarehouse(SimpleDetailWarehouseDTO.of(nextWarehouse));
            ri.setDistanceInMeters(BigDecimal.valueOf(path.getDistance()));
            ri.setTimeInMillis(BigDecimal.valueOf(path.getTime()));
            ri.setGeometry(routingUtils.toWkt(path.getPoints()));
            ri.setInstructions(routingUtils.createInstructions(path.getInstructions()));
            ri.setEstimatedArrival(RoutingUtils.calculateEstimatedArrival(departure, BigDecimal.valueOf(totalTime)));
            routeInfos.add(ri);

            currentLat = nextLat;
            currentLon = nextLon;
            previousWarehouse = nextWarehouse;
        }

        // Summary come primo elemento (stopOrder = 0)
        if (!routeInfos.isEmpty()) {
            MultiRouteInfo summary = new MultiRouteInfo();
            summary.setStopOrder(0);
            summary.setFromWarehouse(SimpleDetailWarehouseDTO.of(originWarehouse));
            summary.setDistanceInMeters(BigDecimal.valueOf(totalDistance));
            summary.setTimeInMillis(BigDecimal.valueOf(totalTime));
            summary.setGeometry("TOTAL_SUMMARY");
            routeInfos.addFirst(summary);
        }

        log.info("calculateMultiDropRoute: completed — total distance {} km", Math.round(totalDistance / 1000));
        return routeInfos;
    }


    private long[][] buildHaversineMatrix(double[] lats, double[] lons, int size) {
        long[][] matrix = new long[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = haversineMeters(lats[i], lons[i], lats[j], lons[j]);
            }
        }
        return matrix;
    }

    private long haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c);
    }

    /**
     * Risolve il TSP con OR-Tools e restituisce l'ordine ottimale degli indici dei magazzini.
     * Il nodo 0 è il deposito (origine), i nodi 1..n sono i magazzini.
     * L'output è una lista di indici 0-based riferiti alla lista dei magazzini.
     */
    private List<Integer> solveWithOrTools(long[][] distanceMatrix, int numWarehouses) {
        int numNodes = numWarehouses + 1; // 0 = deposito
        RoutingIndexManager manager = new RoutingIndexManager(numNodes, 1, 0);
        RoutingModel routing = new RoutingModel(manager);

        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode((int) fromIndex);
            int toNode = manager.indexToNode((int) toIndex);
            return distanceMatrix[fromNode][toNode];
        });
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .build();

        Assignment solution = routing.solveWithParameters(searchParameters);

        List<Integer> optimalOrder = new ArrayList<>();
        long index = routing.start(0);
        index = solution.value(routing.nextVar(index)); // salta il deposito
        while (!routing.isEnd(index)) {
            int node = manager.indexToNode((int) index);
            optimalOrder.add(node - 1); // node 1 → warehouse[0], ecc.
            index = solution.value(routing.nextVar(index));
        }
        return optimalOrder;
    }


    private SingleRouteInfo createRouteInfo(ResponsePath path) {
        SingleRouteInfo singleRouteInfo = new SingleRouteInfo();
        singleRouteInfo.setDistanceInMeters(BigDecimal.valueOf(path.getDistance()));
        singleRouteInfo.setTimeInMillis(BigDecimal.valueOf(path.getTime()));
        singleRouteInfo.setGeometry(routingUtils.toWkt(path.getPoints()));
        singleRouteInfo.setInstructions(routingUtils.createInstructions(path.getInstructions()));
        return singleRouteInfo;
    }

}
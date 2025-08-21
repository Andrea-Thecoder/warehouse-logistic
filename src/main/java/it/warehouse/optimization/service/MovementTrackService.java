package it.warehouse.optimization.service;

import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Transaction;
import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.movementhistory.InsertMovementStatusHistoryDTO;
import it.warehouse.optimization.dto.movementtrack.BaseDetailMovementTrackDTO;
import it.warehouse.optimization.dto.movementtrack.InsertMovementTrackDTO;
import it.warehouse.optimization.dto.routing.RouteInfo;
import it.warehouse.optimization.dto.search.BaseSearchRequest;
import it.warehouse.optimization.dto.search.MovementSearchRequest;
import it.warehouse.optimization.exception.ServiceException;
import it.warehouse.optimization.model.MovementStatusHistory;
import it.warehouse.optimization.model.MovementTrack;
import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.Warehouse;
import it.warehouse.optimization.model.enumerator.MovementStatus;
import it.warehouse.optimization.utils.RoutingUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@Slf4j

public class MovementTrackService {


    @Inject
    Database db;

    @Inject
    MovementStatusHistoryService movementStatusHistoryService;

    @Inject
    StockService stockService;

    @Inject
    WarehouseService warehouseService;

    @Inject
    ProductService productService;

    @Inject
    RoutingService routingService;


    public PagedResultDTO<BaseDetailMovementTrackDTO> findAllMovement(MovementSearchRequest request){
        log.info("findAllMovement: Starting find all movement track");
        ExpressionList<MovementTrack> query = db.find(MovementTrack.class)
                .setLabel("findAllMovement")
                .where();

        request.filterBuilder(query);
        request.pagination(query);

        PagedList<MovementTrack> mtList = query.findPagedList();
        return PagedResultDTO.of(mtList,BaseDetailMovementTrackDTO::of);

    }



    public RouteInfo handleMovementTrack(InsertMovementTrackDTO dto) {
        log.info("handleMovementTrack: Starting create new movement Track for product: {}", dto.getProductId());

        Warehouse originWarehouse = warehouseService.getWarehouseByIdOrThrow(dto.getOriginWarehouseId());
        Warehouse destinationWarehouse = warehouseService.getWarehouseByIdOrThrow(dto.getDestinationWarehouseId());
        Product product = productService.getProductByIdOrThrow(dto.getProductId());
        int quantity = dto.getQuantity();

        try (Transaction tx = db.beginTransaction()) {
            stockService.checkStockAvailability(originWarehouse, product, quantity);

            stockService.checkWarehouseCapacity(destinationWarehouse, product, quantity);

            stockService.decrementStock(originWarehouse, product, quantity, tx);

            RouteInfo route = routingService.calculateRoute(originWarehouse.getCity(), destinationWarehouse.getCity());

            MovementTrack movementTrack = createMovementTrackNoTransaction(dto, route, tx);

            InsertMovementStatusHistoryDTO historyDTO = new InsertMovementStatusHistoryDTO(movementTrack.getId(), movementTrack.getProduct().getId(), movementTrack.getQuantity(), dto.getNotes());

            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.SENT, tx);
            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.IN_TRANSIT, tx);
            tx.commit();
            return route;
        } catch (Exception e) {
            log.error("handleMovementTrack: An error occurred while creating a new movement track. Error message: {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }

    }





    private MovementTrack createMovementTrackNoTransaction(InsertMovementTrackDTO dto, RouteInfo route, Transaction tx) {
        MovementTrack movementTrack = dto.toEntity();
        movementTrack.setEstimatedDistanceMeters(route.getDistanceInMeters());
        movementTrack.setEstimatedDurationMillis(route.getTimeInMillis());
        movementTrack.insert(tx);
        movementTrack.setEstimatedArrival(RoutingUtils.calculateEstimatedArrival(movementTrack.get_dataCreazione(),route.getTimeInMillis()));
        movementTrack.update(tx);
        return movementTrack;
    }


}


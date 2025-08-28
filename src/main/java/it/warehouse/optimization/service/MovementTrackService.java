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
import it.warehouse.optimization.dto.search.MovementSearchRequest;
import it.warehouse.optimization.exception.ServiceException;
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


    public PagedResultDTO<BaseDetailMovementTrackDTO> findAllMovement(MovementSearchRequest request) {
        log.info("findAllMovement: Starting find all movement track");
        ExpressionList<MovementTrack> query = db.find(MovementTrack.class)
                .setLabel("findAllMovement")
                .where();

        request.filterBuilder(query);
        request.pagination(query);

        PagedList<MovementTrack> mtList = query.findPagedList();
        return PagedResultDTO.of(mtList, BaseDetailMovementTrackDTO::of);

    }


    public UUID handleMovementTrack(InsertMovementTrackDTO dto) {
        log.info("handleMovementTrack: Starting create new movement Track for product: {}", dto.getProductId());

        return switch (dto.getMovementStatus()) {
            case MovementStatus.IN_TRANSIT -> handleStatusInTransit(dto);
            case MovementStatus.FROM_FACTORY -> handleStatusFromFactory(dto);
            case MovementStatus.TO_SALE -> handleStatusToSale(dto);
            default -> {
                log.error("handleMovementTrack; Invalid Movement Status. {}", dto.getMovementStatus());
                throw new ServiceException("Invalid Movement Status. Please try again later.");
            }
        };
    }


    public UUID handleStatusReceived(UUID movementTrackId){
        log.info("handleStatusReceived: Status RECEIVED selected.");
        try (Transaction tx = db.beginTransaction()) {
            MovementTrack  movementTrack = getMovementTrackOrThrow(movementTrackId);

            Warehouse destinationWarehouse = movementTrack.getDestinationWarehouse();
            Product product = movementTrack.getProduct();
            int quantity = movementTrack.getQuantity();
            String notes =""; //TODO caprie come mettere le eventuali note qui!

            stockService.increaseStock(destinationWarehouse, product, quantity, tx);

            InsertMovementStatusHistoryDTO historyDTO = new InsertMovementStatusHistoryDTO(movementTrack.getId(), movementTrack.getProduct().getId(), movementTrack.getQuantity(), notes);
            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.RECEIVED, tx);
            tx.commit();
            return movementTrack.getId();
        } catch (Exception e) {
            log.error("handleStatusReceived: An error occurred while creating a new movement track for status RECEIVED. Error message: {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    private UUID handleStatusToSale(InsertMovementTrackDTO dto) {
        log.info("handleStatusToSale: Status TO_SALE selected.");
        try (Transaction tx = db.beginTransaction()) {

            Warehouse originWarehouse = warehouseService.getWarehouseByIdOrThrow(dto.getOriginWarehouseId());
            Product product = productService.getProductByIdOrThrow(dto.getProductId());
            int quantity = dto.getQuantity();

            stockService.decrementStock(originWarehouse, product, quantity, tx);

            MovementTrack movementTrack = createMovementTrackNoTransaction(dto, tx);

            InsertMovementStatusHistoryDTO historyDTO = new InsertMovementStatusHistoryDTO(movementTrack.getId(), movementTrack.getProduct().getId(), movementTrack.getQuantity(), dto.getNotes());

            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.TO_SALE, tx);
            tx.commit();
            return movementTrack.getId();
        } catch (Exception e) {
            log.error("handleStatusToSale: An error occurred while creating a new movement track for status TO_SALE. Error message: {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }


    private UUID handleStatusFromFactory(InsertMovementTrackDTO dto) {
        log.info("handleStatusFromFactory: Status FROM_FACTORY selected.");
        try (Transaction tx = db.beginTransaction()) {

            Warehouse destinationWarehouse = warehouseService.getWarehouseByIdOrThrow(dto.getDestinationWarehouseId());
            Product product = productService.getProductByIdOrThrow(dto.getProductId());
            int quantity = dto.getQuantity();


            stockService.increaseStock(destinationWarehouse,product,quantity,tx);
            MovementTrack movementTrack = createMovementTrackNoTransaction(dto, tx);

            InsertMovementStatusHistoryDTO historyDTO = new InsertMovementStatusHistoryDTO(movementTrack.getId(), movementTrack.getProduct().getId(), movementTrack.getQuantity(), dto.getNotes());

            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.FROM_FACTORY, tx);
            tx.commit();
            return movementTrack.getId();
        } catch (Exception e) {
            log.error("handleStatusFromFactory: An error occurred while creating a new movement track for status FROM_FACTORY. Error message: {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }


    private UUID handleStatusInTransit(InsertMovementTrackDTO dto) {
        log.info("handleStatusInTransit: Status IN_TRANSIT selected.");
        try (Transaction tx = db.beginTransaction()) {

            Warehouse originWarehouse = warehouseService.getWarehouseByIdOrThrow(dto.getOriginWarehouseId());
            Warehouse destinationWarehouse = warehouseService.getWarehouseByIdOrThrow(dto.getDestinationWarehouseId());
            Product product = productService.getProductByIdOrThrow(dto.getProductId());
            int quantity = dto.getQuantity();

            warehouseService.checkWarehouseCapacity(destinationWarehouse,product,quantity);

            stockService.decrementStock(originWarehouse, product, quantity, tx);

            RouteInfo route = routingService.calculateRoute(originWarehouse.getCity(), destinationWarehouse.getCity());

            MovementTrack movementTrack = createMovementTrackNoTransaction(dto, route, tx);

            InsertMovementStatusHistoryDTO historyDTO = new InsertMovementStatusHistoryDTO(movementTrack.getId(), movementTrack.getProduct().getId(), movementTrack.getQuantity(), dto.getNotes());

            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.SENT, tx);
            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.IN_TRANSIT, tx);
            tx.commit();
            return movementTrack.getId();
        } catch (Exception e) {
            log.error("handleStatusInTransit: An error occurred while creating a new movement track for status IN_TRANSIT. Error message: {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }


    private MovementTrack createMovementTrackNoTransaction(InsertMovementTrackDTO dto, RouteInfo route, Transaction tx) {
        MovementTrack movementTrack = dto.toEntity();
        movementTrack.setEstimatedDistanceMeters(route.getDistanceInMeters());
        movementTrack.setEstimatedDurationMillis(route.getTimeInMillis());
        movementTrack.insert(tx);
        movementTrack.setEstimatedArrival(RoutingUtils.calculateEstimatedArrival(movementTrack.get_dataCreazione(), route.getTimeInMillis()));
        movementTrack.update(tx);
        return movementTrack;
    }

    private MovementTrack createMovementTrackNoTransaction(InsertMovementTrackDTO dto, Transaction tx) {
        MovementTrack movementTrack = dto.toEntity();
        movementTrack.insert(tx);
        return movementTrack;
    }


    private MovementTrack getMovementTrackOrThrow(UUID movementTrackId){
        return db.find(MovementTrack.class)
                .setLabel("getMovementTrackOrThrow")
                .where()
                .idEq(movementTrackId)
                .findOneOrEmpty()
                .orElseThrow(()-> {
                    log.error("getMovementTrackOrThrow: Error movement track with ID: {} , not exist",movementTrackId);
                    return new ServiceException("Error movement track with ID: "+ movementTrackId +" , not exist");
                });
    }

}


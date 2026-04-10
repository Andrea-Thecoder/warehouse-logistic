package it.warehouse.optimization.service;

import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Transaction;
import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.movementdestination.InsertMovementDestinationDTO;
import it.warehouse.optimization.dto.movementhistory.InsertMovementStatusHistoryDTO;
import it.warehouse.optimization.dto.movementtrack.BaseDetailMovementTrackDTO;
import it.warehouse.optimization.dto.movementtrack.InsertMovementTrackDTO;
import it.warehouse.optimization.dto.movementtrack.ReceivedMovementTrackDTO;
import it.warehouse.optimization.dto.routing.MultiRouteInfo;
import it.warehouse.optimization.dto.search.MovementSearchRequest;
import it.warehouse.optimization.exception.ServiceException;
import it.warehouse.optimization.model.MovementTrack;
import it.warehouse.optimization.model.MovementTrackDestination;
import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.Warehouse;
import it.warehouse.optimization.model.enumerator.MovementStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

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

    private static final String DEFAULT_RECEIVED_NOTE = "Prodotto ricevuto.";
    private static final String DEFAULT_CANCELLED_NOTE = "Movimento cancellato.";


    public PagedResultDTO<BaseDetailMovementTrackDTO> findAllMovement(MovementSearchRequest request) {
        log.info("findAllMovement: Starting find all movement track");
        ExpressionList<MovementTrack> query = db.find(MovementTrack.class)
                .setLabel("findAllMovement")
                .fetch("destinations")
                .fetch("destinations.destinationWarehouse", "id, name")
                .where();

        request.filterBuilder(query);
        request.pagination(query);

        PagedList<MovementTrack> mtList = query.findPagedList();
        return PagedResultDTO.of(mtList, BaseDetailMovementTrackDTO::of);

    }


    public UUID handleMovementTrack(InsertMovementTrackDTO dto) {
        log.info("handleMovementTrack: Starting create new movement Track for product: {}", dto.getProductId());
        MovementStatus statusInput = dto.getMovementStatus();
        if (!MovementStatus.VALID_RECEIVED_STATUS.contains(statusInput)) {
            log.error("handleMovementTrack; Invalid Movement Status. {}", dto.getMovementStatus());
            throw new ServiceException("Invalid Movement Status. Please try again later.");
        }

        if (MovementStatus.IN_TRANSIT.equals(statusInput))
            return handleStatusInTransit(dto);

        return handleStatusToSale(dto);
    }


    public UUID handleStatusReceived(UUID movementTrackId, ReceivedMovementTrackDTO dto) {
        log.info("handleStatusReceived:  Update status for MovementTrack: {}.", movementTrackId);
        MovementTrack movementTrack = getMovementTrackOrThrow(movementTrackId);
        if (!validateReceivedQuantity(dto, movementTrack.getQuantity())) {
            log.error("handleStatusReceived: Received quantity inconsistent with declared status.");
            throw new ServiceException("Received quantity inconsistent with declared status.");
        }
        Warehouse destinationWh = warehouseService.getWarehouseByIdOrThrow(dto.getDestinationWarehouseId());
        try (Transaction tx = db.beginTransaction()) {
            Product product = movementTrack.getProduct();
            int quantity = dto.getReceivedQuantity();
            String notes = ObjectUtils.firstNonNull(dto.getNotes(), DEFAULT_RECEIVED_NOTE);

            warehouseService.checkWarehouseCapacity(destinationWh, product, quantity);
            stockService.increaseStock(destinationWh, product, quantity, tx);

            movementTrack.setStatus(dto.getMovementStatus());
            movementTrack.update(tx);

            InsertMovementStatusHistoryDTO historyDTO = new InsertMovementStatusHistoryDTO(movementTrack.getId(), product.getId(), quantity, notes);
            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.RECEIVED, tx);
            tx.commit();
            return movementTrack.getId();
        } catch (Exception e) {
            log.error("handleStatusReceived: An error occurred while updating a movement track. Error message: {}", e.getMessage());
            throw new ServiceException("Error while update status. Try again later.");
        }
    }

    public void handleStatusCancelled(UUID movementTrackId, String notes) {
        log.info("handleStatusCancelled:  Update status for MovementTrack: {}.", movementTrackId);
        MovementTrack movementTrack = getMovementTrackOrThrow(movementTrackId);
        MovementStatus actualStatus = movementTrack.getStatus();
        if (!MovementStatus.VALID_INSERT_STATUS.contains(actualStatus)) {
            log.error("handleStatusCancelled: Actual status ({}) can not be cancelled.", actualStatus);
            throw new ServiceException("This movement can not be cancelled.");
        }
        try (Transaction tx = db.beginTransaction()) {
            stockService.increaseStock(movementTrack.getOriginWarehouse(), movementTrack.getProduct(), movementTrack.getQuantity(), tx);

            movementTrack.setStatus(MovementStatus.CANCELLED);
            movementTrack.update(tx);
            InsertMovementStatusHistoryDTO historyDTO = new InsertMovementStatusHistoryDTO(movementTrack.getId(), movementTrack.getProduct().getId(), movementTrack.getQuantity(), ObjectUtils.firstNonNull(notes, DEFAULT_CANCELLED_NOTE));
            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.CANCELLED, tx);
            tx.commit();

        } catch (Exception e) {
            log.error("handleStatusCancelled: An error occurred while set cancelled movement track. Error message: {}", e.getMessage());
            throw new ServiceException("Error while cancelling movement. Try again later.");
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


    private UUID handleStatusInTransit(InsertMovementTrackDTO dto) {
        log.info("handleStatusInTransit: Status IN_TRANSIT selected.");
        try (Transaction tx = db.beginTransaction()) {

            // 1. Carica magazzino origine e prodotto
            Warehouse originWarehouse = warehouseService.getWarehouseByIdOrThrow(dto.getOriginWarehouseId());
            Product product = productService.getProductByIdOrThrow(dto.getProductId());

            // 2. Costruisce mappa destinationWarehouseId → DTO e Set<Warehouse> per il routing
            Map<UUID, InsertMovementDestinationDTO> destinationDTOMap = new HashMap<>();
            Set<Warehouse> destinationWarehouses = new HashSet<>();
            for (InsertMovementDestinationDTO destDTO : dto.getDestinations()) {
                Warehouse w = warehouseService.getWarehouseByIdOrThrow(destDTO.getDestinationWarehouseId());
                destinationWarehouses.add(w);
                destinationDTOMap.put(w.getId(), destDTO);
            }

            // 3. Decrementa stock all'origine per la quantità totale
            stockService.decrementStock(originWarehouse, product, dto.getQuantity(), tx);

            // 4. Calcola rotta ottimale multi-tappa (OR-Tools + GraphHopper)
            List<MultiRouteInfo> route = routingService.calculateMultiDropRoute(originWarehouse, destinationWarehouses);

            // 5. Crea MovementTrack con i totali del routing (richiesti dalla constraint chk_origin_duration_distance)
            MultiRouteInfo summary = route.getFirst(); // stopOrder=0 è sempre il summary
            MovementTrack movementTrack = dto.toEntity();
            movementTrack.setStatus(MovementStatus.IN_TRANSIT);
            movementTrack.setEstimatedTotalDistanceMeters(summary.getDistanceInMeters());
            movementTrack.setEstimatedTotalDurationMillis(summary.getTimeInMillis());
            movementTrack.insert(tx);

            // 6. Crea MovementTrackDestination per ogni tappa (salta summary a indice 0)
            for (MultiRouteInfo leg : route) {
                if (leg.getStopOrder() == 0) continue;
                UUID destWarehouseId = leg.getToWarehouse().getId();
                InsertMovementDestinationDTO destDTO = destinationDTOMap.get(destWarehouseId);

                MovementTrackDestination destination = destDTO.toEntity();
                destination.setMovementTrack(movementTrack);
                destination.setStopOrder(leg.getStopOrder());
                destination.setEstimatedDistanceMeters(leg.getDistanceInMeters());
                destination.setEstimatedDurationMillis(leg.getTimeInMillis());
                destination.setEstimatedArrival(leg.getEstimatedArrival());
                destination.insert(tx);
            }

            // 7. Crea history SENT e IN_TRANSIT
            InsertMovementStatusHistoryDTO historyDTO = new InsertMovementStatusHistoryDTO(movementTrack.getId(), product.getId(), dto.getQuantity(), dto.getNotes());
            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.SENT, tx);
            movementStatusHistoryService.createMovementHistory(historyDTO, MovementStatus.IN_TRANSIT, tx);

            tx.commit();
            return movementTrack.getId();
        } catch (Exception e) {
            log.error("handleStatusInTransit: An error occurred while creating a new movement track for status IN_TRANSIT. Error message: {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }


    private MovementTrack createMovementTrackNoTransaction(InsertMovementTrackDTO dto, Transaction tx) {
        MovementTrack movementTrack = dto.toEntity();
        movementTrack.insert(tx);
        return movementTrack;
    }


    private boolean validateReceivedQuantity(ReceivedMovementTrackDTO dto, int expectedQuantity) {
        boolean isReceived = expectedQuantity == dto.getReceivedQuantity() && MovementStatus.RECEIVED.equals(dto.getMovementStatus());
        boolean isPartiallyReceived = expectedQuantity > dto.getReceivedQuantity() && MovementStatus.PARTIALLY_RECEIVED.equals(dto.getMovementStatus());
        boolean isOverReceived = expectedQuantity < dto.getReceivedQuantity() && MovementStatus.OVER_RECEIVED.equals(dto.getMovementStatus());
        return isReceived || isPartiallyReceived || isOverReceived;
    }

    private MovementTrack getMovementTrackOrThrow(UUID movementTrackId) {
        return db.find(MovementTrack.class)
                .setLabel("getMovementTrackOrThrow")
                .where()
                .idEq(movementTrackId)
                .findOneOrEmpty()
                .orElseThrow(() -> {
                    log.error("getMovementTrackOrThrow: Error movement track with ID: {} , not exist", movementTrackId);
                    return new ServiceException("Error movement track with ID: " + movementTrackId + " , not exist");
                });
    }


}


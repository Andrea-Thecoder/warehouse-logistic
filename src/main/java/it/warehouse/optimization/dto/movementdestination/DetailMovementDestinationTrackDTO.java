package it.warehouse.optimization.dto.movementdestination;


import it.warehouse.optimization.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.optimization.model.MovementTrackDestination;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class DetailMovementDestinationTrackDTO {

    private UUID id;
    private SimpleDetailWarehouseDTO destinationWarehouse;
    private Integer quantityDelivered;
    private Integer stopOrder;
    private BigDecimal estimatedDurationMillis;
    private BigDecimal estimatedDistanceMeters;
    private LocalDateTime estimatedArrival;
    private String notes;

    public static DetailMovementDestinationTrackDTO of(MovementTrackDestination entity) {
        DetailMovementDestinationTrackDTO dto = new DetailMovementDestinationTrackDTO();
        dto.setId(entity.getId());
        dto.setDestinationWarehouse(SimpleDetailWarehouseDTO.of(entity.getDestinationWarehouse()));
        dto.setQuantityDelivered(entity.getQuantityDelivered());
        dto.setStopOrder(entity.getStopOrder());
        dto.setEstimatedDurationMillis(entity.getEstimatedDurationMillis());
        dto.setEstimatedDistanceMeters(entity.getEstimatedDistanceMeters());
        dto.setEstimatedArrival(entity.getEstimatedArrival());
        dto.setNotes(entity.getNotes());
        return dto;
    }

}

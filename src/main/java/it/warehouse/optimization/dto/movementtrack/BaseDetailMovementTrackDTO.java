package it.warehouse.optimization.dto.movementtrack;

import it.warehouse.optimization.dto.product.SimpleDetailProductDTO;
import it.warehouse.optimization.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.optimization.model.MovementTrack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public final class BaseDetailMovementTrackDTO  extends BaseMovementTrackDTO {

    private List<String> destinationsWarehouseName;


    public static BaseDetailMovementTrackDTO of(MovementTrack mt) {
        BaseDetailMovementTrackDTO dto = new BaseDetailMovementTrackDTO();
        dto.setId(mt.getId());
        dto.setOriginWarehouse(SimpleDetailWarehouseDTO.of(mt.getOriginWarehouse()));
        dto.setProduct(SimpleDetailProductDTO.of(mt.getProduct()));
        dto.setQuantity(mt.getQuantity());
        dto.setEstimatedArrival(mt.getEstimatedFinalDateForFinalTravel());
        dto.setDestinationsWarehouseName(
            mt.getDestinations().stream()
                .map(mtd -> mtd.getDestinationWarehouse().getName())
                .collect(Collectors.toList())
        );
        return dto;
    }

}

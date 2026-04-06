package it.warehouse.optimization.dto.movementtrack;

import it.warehouse.optimization.dto.movementdestination.DetailMovementDestinationTrackDTO;
import it.warehouse.optimization.dto.product.SimpleDetailProductDTO;
import it.warehouse.optimization.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.optimization.model.MovementTrack;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter

public final class DetailMovementTrackDTO extends BaseMovementTrackDTO {

    private List<DetailMovementDestinationTrackDTO> destinations;

    public static DetailMovementTrackDTO of(MovementTrack mt) {
        DetailMovementTrackDTO dto = new DetailMovementTrackDTO();
        dto.setId(mt.getId());
        dto.setOriginWarehouse(SimpleDetailWarehouseDTO.of(mt.getOriginWarehouse()));
        dto.setProduct(SimpleDetailProductDTO.of(mt.getProduct()));
        dto.setQuantity(mt.getQuantity());
        dto.setEstimatedArrival(mt.getEstimatedFinalDateForFinalTravel());
        dto.setDestinations(
                mt.getDestinations().stream()
                        .map(DetailMovementDestinationTrackDTO::of)
                        .collect(Collectors.toList())
        );
        return dto;
    }

}

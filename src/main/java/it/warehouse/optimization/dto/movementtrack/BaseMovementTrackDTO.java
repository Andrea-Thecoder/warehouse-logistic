package it.warehouse.optimization.dto.movementtrack;

import it.warehouse.optimization.dto.product.SimpleDetailProductDTO;
import it.warehouse.optimization.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.optimization.model.MovementTrack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public sealed class BaseMovementTrackDTO permits DetailMovementTrackDTO, BaseDetailMovementTrackDTO {

    protected UUID id;
    protected SimpleDetailWarehouseDTO originWarehouse;
    protected SimpleDetailProductDTO product;
    protected Integer quantity;
    protected LocalDateTime estimatedArrival;


}

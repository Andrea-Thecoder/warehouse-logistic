package it.warehouse.optimization.dto.movementtrack;

import it.warehouse.optimization.dto.product.SimpleDetailProductDTO;
import it.warehouse.optimization.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.optimization.model.MovementTrack;
import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.Warehouse;
import jakarta.persistence.*;
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

public class BaseDetailMovementTrackDTO {


    protected UUID id;
    protected SimpleDetailWarehouseDTO originWarehouse;
    protected SimpleDetailWarehouseDTO destinationWarehouse;
    protected SimpleDetailProductDTO product;
    protected Integer quantity;
    protected LocalDateTime estimatedArrival;


    public static BaseDetailMovementTrackDTO of (MovementTrack mt){
        BaseDetailMovementTrackDTO dto = new BaseDetailMovementTrackDTO();
        dto.setId(mt.getId());
        dto.setOriginWarehouse(SimpleDetailWarehouseDTO.of(mt.getOriginWarehouse()));
        dto.setDestinationWarehouse(SimpleDetailWarehouseDTO.of(mt.getDestinationWarehouse()));
        dto.setProduct(SimpleDetailProductDTO.of(mt.getProduct()));
        dto.setQuantity(mt.getQuantity());
        dto.setEstimatedArrival(mt.getEstimatedArrival());
        return dto;
    }

}

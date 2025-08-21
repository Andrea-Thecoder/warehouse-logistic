package it.warehouse.optimization.dto.movementtrack;


import it.warehouse.optimization.model.MovementTrack;
import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.Warehouse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class InsertMovementTrackDTO {

    @NotNull(message = "Origin warehouse ID must be valorized.")
    private UUID originWarehouseId;

    @NotNull(message = "Destination warehouse ID must be valorized.")
    private UUID destinationWarehouseId;

    @NotNull(message = "Product ID must be valorized.")
    private UUID productId;

    @NotNull(message = "Quantity must be valorized.")
    @Positive(message = "Quantity must be positive.")
    private Integer quantity;

    private String notes;


    public MovementTrack toEntity(){
        MovementTrack mt = new MovementTrack();
        mt.setOriginWarehouse(mt.db().reference(Warehouse.class,originWarehouseId));
        mt.setDestinationWarehouse(mt.db().reference(Warehouse.class,destinationWarehouseId));
        mt.setProduct(mt.db().reference(Product.class,productId));
        mt.setQuantity(quantity);
        return mt;
    }

}

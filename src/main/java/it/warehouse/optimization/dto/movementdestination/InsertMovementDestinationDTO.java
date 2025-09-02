package it.warehouse.optimization.dto.movementdestination;

import it.warehouse.optimization.model.MovementTrackDestination;
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

public class InsertMovementDestinationDTO {

    @NotNull(message = "Destination Warehouse ID must be valorized.")
    private UUID destinationWarehouseId;

    @NotNull(message = "Quantity must be valorized.")
    @Positive(message = "Quantity must be a positive value")
    private Integer quantity;

    private String notes;


    public MovementTrackDestination toEntity (){
        MovementTrackDestination mtd = new MovementTrackDestination();
        mtd.setQuantityDelivered(quantity);
        mtd.setNotes(notes);
        mtd.setDestinationWarehouse(mtd.db().reference(Warehouse.class,destinationWarehouseId));
        return mtd;
    }

    @Override
    public boolean equals(Object o){
        if (this == o ) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        InsertMovementDestinationDTO that = (InsertMovementDestinationDTO) o;
        return destinationWarehouseId != null && destinationWarehouseId.equals(that.getDestinationWarehouseId());
    }


    @Override
    public int hashCode(){
        return destinationWarehouseId != null ? destinationWarehouseId.hashCode() : 0;
    }
}

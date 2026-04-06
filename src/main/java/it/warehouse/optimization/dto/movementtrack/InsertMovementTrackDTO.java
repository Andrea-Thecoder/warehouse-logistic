package it.warehouse.optimization.dto.movementtrack;


import com.fasterxml.jackson.annotation.JsonIgnore;
import it.warehouse.optimization.dto.movementdestination.InsertMovementDestinationDTO;
import it.warehouse.optimization.model.MovementTrack;
import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.Warehouse;
import it.warehouse.optimization.model.enumerator.MovementStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class InsertMovementTrackDTO {

    @NotNull(message = "Movement Status must be valorized.")
    private MovementStatus movementStatus;

    private UUID originWarehouseId;

    @Valid
    private Set<InsertMovementDestinationDTO> destinations;

    @NotNull(message = "Product ID must be valorized.")
    private UUID productId;

    @NotNull(message = "Quantity must be valorized.")
    @Positive(message = "Quantity must be positive.")
    private Integer quantity;

    private String notes;

    @AssertTrue(message = "Movement Status not authorized for this operation.")
    @JsonIgnore
    public boolean isValidMovementStatus(){
        return movementStatus != null && MovementStatus.VALID_INSERT_STATUS.contains(movementStatus);
    }

    @AssertTrue(message = "Either originWarehouseId or destinationWarehouseId must be provided")
    @JsonIgnore
    public boolean isValidWarehouses() {
        boolean hasOrigin = originWarehouseId != null;
        boolean hasOneDestination = CollectionUtils.isNotEmpty(destinations) && destinations.size() == 1;
        return hasOrigin || hasOneDestination;
    }

    @AssertTrue(message = "Both originWarehouseId destinationWarehouseId must be valorized.")
    @JsonIgnore
    public boolean isValidTransitWarehouse() {
        if (movementStatus == MovementStatus.IN_TRANSIT)
            return originWarehouseId != null && CollectionUtils.isNotEmpty(destinations);
        return true;
    }

    public MovementTrack toEntity() {
        MovementTrack mt = new MovementTrack();
        if (originWarehouseId != null)
            mt.setOriginWarehouse(mt.db().reference(Warehouse.class, originWarehouseId));
        mt.setStatus(movementStatus);
        mt.setProduct(mt.db().reference(Product.class, productId));
        mt.setQuantity(quantity);
        return mt;
    }

}

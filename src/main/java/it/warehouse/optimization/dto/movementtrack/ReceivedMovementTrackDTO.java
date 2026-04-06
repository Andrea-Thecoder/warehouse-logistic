package it.warehouse.optimization.dto.movementtrack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.warehouse.optimization.model.enumerator.MovementStatus;
import jakarta.validation.constraints.AssertTrue;
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
public class ReceivedMovementTrackDTO {

    @NotNull(message = "Destination warehouse ID must be valorized.")
    private UUID destinationWarehouseId;

    @NotNull(message = "Movement Status must be valorized.")
    private MovementStatus movementStatus;

    @NotNull(message = "Received Quantity must be valorized.")
    @Positive(message = "Received Quantity must be positive.")
    private Integer receivedQuantity;

    private String notes;


    @AssertTrue(message = "Movement Status not authorized for this operation.")
    @JsonIgnore
    public boolean isValidMovementStatus() {
        return movementStatus != null && MovementStatus.VALID_RECEIVED_STATUS.contains(movementStatus);
    }

}
package it.warehouse.optimization.dto.movementtrack;

import jakarta.validation.constraints.NotNull;
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

    private String notes;

}
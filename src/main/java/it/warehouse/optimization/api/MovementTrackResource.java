package it.warehouse.optimization.api;


import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.SimpleResultDTO;
import it.warehouse.optimization.dto.movementtrack.BaseDetailMovementTrackDTO;
import it.warehouse.optimization.dto.movementtrack.InsertMovementTrackDTO;
import it.warehouse.optimization.dto.movementtrack.ReceivedMovementTrackDTO;
import it.warehouse.optimization.dto.search.MovementSearchRequest;
import it.warehouse.optimization.service.MovementTrackService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "API Stock Movement")
@Path("movement-track")
@Slf4j

public class MovementTrackResource {

    @Inject
    MovementTrackService movementTrackService;

    @POST
    @Operation(
            summary = "Insert movement track.",
            description = "API for insert a new movement for a product"
    )
    public SimpleResultDTO<UUID> insertMovementTrack(
            @Valid InsertMovementTrackDTO dto
            ){
        log.info("MovementTrackResource - insertMovementTrack");
        return SimpleResultDTO.<UUID>builder()
                .payload(movementTrackService.handleMovementTrack(dto))
                .message("Movement track created successfully.")
                .build();
    }

    @PATCH
    @Path("/{movementTrackId}/received")
    @Operation(
            summary = "Mark movement as received.",
            description = "Registers the reception of a movement at the destination warehouse and updates stock accordingly."
    )
    public SimpleResultDTO<UUID> receivedMovementTrack(
            @PathParam("movementTrackId") UUID movementTrackId,
            @Valid ReceivedMovementTrackDTO dto
    ) {
        log.info("MovementTrackResource - receivedMovementTrack");
        return SimpleResultDTO.<UUID>builder()
                .payload(movementTrackService.handleStatusReceived(movementTrackId, dto))
                .message("Movement track received successfully.")
                .build();
    }

    @PATCH
    @Path("/{movementTrackId}/cancelled")
    @Operation(
            summary = "Cancel a movement track.",
            description = "Cancels an existing movement and compensates stock accordingly. Only applicable to IN_TRANSIT and TO_SALE statuses."
    )
    public SimpleResultDTO<Void> cancelledMovementTrack(
            @PathParam("movementTrackId") UUID movementTrackId,
            @QueryParam("notes") String notes
    ) {
        log.info("MovementTrackResource - cancelledMovementTrack");
        movementTrackService.handleStatusCancelled(movementTrackId, notes);
        return SimpleResultDTO.<Void>builder()
                .message("Movement track cancelled successfully.")
                .build();
    }

    @GET
    @Operation(
            summary = "Find movement track",
            description = "API for all movement track on database. Can apply advanced filter. "
    )
    public PagedResultDTO<BaseDetailMovementTrackDTO> findAllMovement(
            @BeanParam @Valid MovementSearchRequest request
            ){
        log.info("MovementTrackResource - findAllMovement");
        return movementTrackService.findAllMovement(request);
    }

}

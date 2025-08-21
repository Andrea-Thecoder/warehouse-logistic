package it.warehouse.optimization.api;


import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.movementtrack.BaseDetailMovementTrackDTO;
import it.warehouse.optimization.dto.movementtrack.InsertMovementTrackDTO;
import it.warehouse.optimization.dto.routing.RouteInfo;
import it.warehouse.optimization.dto.search.MovementSearchRequest;
import it.warehouse.optimization.service.MovementTrackService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "API Routing Calculator")
@Path("routing")
@Slf4j

public class MovementTrackResource {

    @Inject
    MovementTrackService movementTrackService;

    @POST
    @Operation(
            summary = "Insert movement track.",
            description = "API for insert a new movement for a product"
    )
    public RouteInfo insertMovementTrack(
            @Valid InsertMovementTrackDTO dto
            ){
        log.info("MovementTrackResource - insertMovementTrack");
        return movementTrackService.handleMovementTrack(dto);
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

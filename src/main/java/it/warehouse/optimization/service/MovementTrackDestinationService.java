package it.warehouse.optimization.service;

import io.ebean.Database;
import io.ebean.Transaction;
import it.warehouse.optimization.dto.movementdestination.InsertMovementDestinationDTO;
import it.warehouse.optimization.model.MovementTrack;
import it.warehouse.optimization.model.MovementTrackDestination;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@Slf4j

public class MovementTrackDestinationService {

    @Inject
    Database db;

    @Inject
    RoutingService routingService;


    public UUID createMovementDestinationNoTransaction(InsertMovementDestinationDTO dto, MovementTrack movementTrack, Transaction tx){
        MovementTrackDestination mtd = dto.toEntity();
        mtd.setMovementTrack(movementTrack);
        return mtd.getId();

    }
}

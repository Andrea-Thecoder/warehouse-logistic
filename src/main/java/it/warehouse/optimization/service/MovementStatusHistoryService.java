package it.warehouse.optimization.service;

import io.ebean.Database;
import io.ebean.Transaction;
import it.warehouse.optimization.dto.movementhistory.InsertMovementStatusHistoryDTO;
import it.warehouse.optimization.model.MovementStatusHistory;
import it.warehouse.optimization.model.MovementTrack;
import it.warehouse.optimization.model.enumerator.MovementStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@Slf4j

public class MovementStatusHistoryService {

    @Inject
    Database db;



  /*  private void createMovementHistory(
            UUID movementTrackId, UUID productId,
            Integer quantity, MovementStatus status,
            String notes, Transaction tx){
        InsertMovementStatusHistoryDTO historyDTO = new InsertMovementStatusHistoryDTO(movementTrack.getId(),movementTrack.getProduct().getId(),movementTrack.getQuantity(),notes);
        movementStatusHistoryService.createMovementHistory(historyDTO,MovementStatus.SENT,tx);
        movementStatusHistoryService.createMovementHistory(historyDTO,MovementStatus.IN_TRANSIT,tx);
*/


    public void createMovementHistory(@Valid InsertMovementStatusHistoryDTO dto, MovementStatus status, Transaction tx){
        log.info("createMovementHistory: Starting create movement status history for movement track id: {}",dto.getMovementTrackId());
        MovementStatusHistory msh = dto.toEntity();
        msh.setStatus(status);
        msh.insert(tx);
    }
}

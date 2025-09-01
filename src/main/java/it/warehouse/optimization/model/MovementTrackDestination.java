package it.warehouse.optimization.model;

import io.ebean.annotation.Index;
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
@Entity
@Table(name ="a_movement_track_destination")
public class MovementTrackDestination  extends  AbstractAuditable{

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    @Index
    private Warehouse destinationWarehouse;

    @ManyToOne
    @JoinColumn(name = "movement_track_id", nullable = false)
    private MovementTrack movementTrack;

    @Column(nullable = false)
    private Integer quantityDelivered;

    @Column(nullable = false)
    private Integer stopOrder;

    @Column(precision =  12)
    private BigDecimal estimatedDurationMillis;

    @Column(precision =  19, scale = 10)
    private BigDecimal estimatedDistanceMeters;

    @Column
    private LocalDateTime estimatedArrival;

    @Column(columnDefinition = "TEXT")
    private String notes;


}

package it.warehouse.optimization.model;

import it.warehouse.optimization.model.enumerator.MovementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.swing.text.Position;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movementtrack")

public class MovementTrack  extends  AbstractAuditable{

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "origin_warehouse_id", nullable = false)
    private Warehouse originWarehouse;

    @ManyToOne
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision =  12,nullable = false)
    private BigDecimal estimatedDurationMillis;

    @Column(precision =  19, scale = 10,nullable = false)
    private BigDecimal estimatedDistanceMeters;

    @Column
    private LocalDateTime estimatedArrival;

    @Column
    private String veicoloDaImplementare;


}

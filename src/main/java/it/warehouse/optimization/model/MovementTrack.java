package it.warehouse.optimization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movement_track")

public class MovementTrack  extends  AbstractAuditable{

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "origin_warehouse_id")
    private Warehouse originWarehouse;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementStatus status;

    @Column(precision =  12)
    private BigDecimal estimatedTotalDurationMillis;

    @Column(precision =  19, scale = 10)
    private BigDecimal estimatedTotalDistanceMeters;

    @Column
    private LocalDateTime estimatedFinalDateForFinalTravel;

    @Column
    private String veicoloDaImplementare;

    @JsonIgnore
    @OneToMany(mappedBy = "movementTrack")
    private List<MovementTrackDestination> destinations;


}

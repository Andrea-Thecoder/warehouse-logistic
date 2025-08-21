package it.warehouse.optimization.model;


import it.warehouse.optimization.model.enumerator.MovementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movement_status_history")

public class MovementStatusHistory  extends  AbstractAuditable{

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movement_track_id", nullable = false)
    private MovementTrack movementTrack;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MovementStatus status;

    @Column(nullable = false)
    private Integer quantity;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

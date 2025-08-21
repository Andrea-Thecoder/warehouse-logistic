package it.warehouse.optimization.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "warehouse")
public class Warehouse extends  AbstractAuditable{

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String name;

    @Column(nullable = false)
    private Double volumeCapacity;

    @Column(nullable = false)
    private Double weightCapacity;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

}

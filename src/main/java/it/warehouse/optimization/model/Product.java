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
@Table(name = "product")


public class Product extends  AbstractAuditable{

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String name;

    @Column(nullable = false)
    private Double volume;

    @Column(nullable = false)
    private Double weight;

    @ManyToOne
    @JoinColumn(name = "movement_type_id", nullable = false)
    private CategoryType category;

}

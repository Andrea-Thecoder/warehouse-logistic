package it.warehouse.optimization.model;

import io.ebean.annotation.Index;
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
@Table(name = "region")

public class Region {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    @Index
    private String name;

    @Column(length = 10,nullable = false,unique = true)
    private String istatCode;

}
